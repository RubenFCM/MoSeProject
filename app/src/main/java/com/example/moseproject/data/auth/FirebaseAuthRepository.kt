package com.example.moseproject.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthRepository {
    private val requests = Mutex()

    override val users = callbackFlow {
        val listener = FirebaseAuth.IdTokenListener { firebase ->
            trySend(firebase.currentUser?.snapshot())
        }
        auth.addIdTokenListener(listener)
        awaitClose { auth.removeIdTokenListener(listener) }
    }.distinctUntilChanged()

    override suspend fun refreshSession(): AuthSession = request {
        val user = auth.currentUser ?: return@request AuthSession.SignedOut
        user.reload().await()
        // Force new claims after verification (or restoration); never expose or retain the token.
        val result = user.getIdToken(true).await()
        if (auth.currentUser?.uid != user.uid) throw AuthException(AuthFailure.SessionExpired)
        val snapshot = user.snapshot()
        if (snapshot.emailVerified && result.claims["email_verified"] == true) {
            AuthSession.Authenticated(snapshot)
        } else {
            AuthSession.PendingVerification(snapshot)
        }
    }

    override suspend fun signIn(email: String, password: String) = request {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun register(email: String, password: String) = request {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun sendVerification() = request {
        val user = auth.currentUser ?: throw AuthException(AuthFailure.SessionExpired)
        auth.setLanguageCode("es")
        user.sendEmailVerification().await()
        Unit
    }

    override suspend fun sendPasswordReset(email: String) = request {
        auth.setLanguageCode("es")
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (_: FirebaseAuthInvalidUserException) {
            // Same success response for unknown addresses, including projects without enumeration protection.
        }
        Unit
    }

    override suspend fun signOut() = request { auth.signOut() }

    /**
     * Firebase Tasks cannot be cancelled. Finish each SDK operation under the mutex, even when
     * its UI is disposed, so a late sign-in cannot undo a subsequent sign-out. No detached jobs.
     */
    private suspend fun <T> request(block: suspend () -> T): T = withContext(NonCancellable) {
        requests.withLock {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: FirebaseAuthInvalidUserException) {
                // A deleted/disabled user or an invalid refresh token must not retain an account screen.
                auth.signOut()
                throw AuthException(AuthFailure.SessionExpired)
            } catch (error: Exception) {
                throw AuthException(error.toAuthFailure())
            }
        }
    }
}

private fun FirebaseUser.snapshot() = AuthUser(uid, email.orEmpty(), isEmailVerified)

internal fun Exception.toAuthFailure(): AuthFailure = when (this) {
    is AuthException -> failure
    is FirebaseNetworkException -> AuthFailure.Network
    is FirebaseTooManyRequestsException -> AuthFailure.RateLimited
    is FirebaseAuthWeakPasswordException -> AuthFailure.PasswordPolicy
    is FirebaseAuthUserCollisionException -> AuthFailure.Registration
    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthFailure.Credentials
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_OPERATION_NOT_ALLOWED", "ERROR_INVALID_API_KEY", "ERROR_APP_NOT_AUTHORIZED", "ERROR_INVALID_APP_CREDENTIAL" -> AuthFailure.Configuration
        "ERROR_PASSWORD_DOES_NOT_MEET_REQUIREMENTS" -> AuthFailure.PasswordPolicy
        "ERROR_TOO_MANY_REQUESTS" -> AuthFailure.RateLimited
        "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> AuthFailure.Credentials
        else -> AuthFailure.Unknown
    }
    else -> AuthFailure.Unknown
}
