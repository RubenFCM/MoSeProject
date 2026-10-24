package com.example.moseproject.data.auth

import android.content.Context
import com.example.moseproject.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object AuthContainer {
    fun createRepository(context: Context): AuthRepository {
        // Use the existing default Firebase app/resources; there are no synthetic project IDs.
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(context)
            ?: return MissingFirebaseConfiguration()
        val auth = FirebaseAuth.getInstance(app)
        if (BuildConfig.DEBUG && BuildConfig.AUTH_USE_EMULATOR) auth.useEmulator("10.0.2.2", 9099)
        return FirebaseAuthRepository(auth)
    }

    val passwordPolicy = PasswordPolicy(
        minLength = BuildConfig.AUTH_PASSWORD_MIN_LENGTH,
        maxLength = BuildConfig.AUTH_PASSWORD_MAX_LENGTH,
        lowercase = BuildConfig.AUTH_PASSWORD_REQUIRE_LOWERCASE,
        uppercase = BuildConfig.AUTH_PASSWORD_REQUIRE_UPPERCASE,
        number = BuildConfig.AUTH_PASSWORD_REQUIRE_NUMBER,
        symbol = BuildConfig.AUTH_PASSWORD_REQUIRE_SYMBOL
    )
}

/** An explicit unavailable state, never a mock login or a local replacement for Firebase. */
private class MissingFirebaseConfiguration : AuthRepository {
    override val users: Flow<AuthUser?> = flow { throw AuthException(AuthFailure.Configuration) }
    private fun unavailable(): Nothing = throw AuthException(AuthFailure.Configuration)
    override suspend fun refreshSession(): AuthSession = unavailable()
    override suspend fun signIn(email: String, password: String): Unit = unavailable()
    override suspend fun register(email: String, password: String): Unit = unavailable()
    override suspend fun sendVerification(): Unit = unavailable()
    override suspend fun sendPasswordReset(email: String): Unit = unavailable()
    override suspend fun signOut(): Unit = unavailable()
}
