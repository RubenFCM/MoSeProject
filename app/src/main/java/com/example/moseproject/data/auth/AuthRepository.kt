package com.example.moseproject.data.auth

import kotlinx.coroutines.flow.Flow

data class AuthUser(val uid: String, val email: String, val emailVerified: Boolean)

sealed interface AuthSession {
    data object Initializing : AuthSession
    data object SignedOut : AuthSession
    data class PendingVerification(val user: AuthUser) : AuthSession
    data class Authenticated(val user: AuthUser) : AuthSession
    data class Unavailable(val reason: AuthFailure) : AuthSession
}

enum class AuthFailure { Configuration, Network, Credentials, Registration, PasswordPolicy, RateLimited, SessionExpired, Unknown }
class AuthException(val failure: AuthFailure) : Exception()

interface AuthRepository {
    /** SDK snapshots are signals, not authorization. refreshSession validates the user and token. */
    val users: Flow<AuthUser?>
    suspend fun refreshSession(): AuthSession
    suspend fun signIn(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun sendVerification()
    suspend fun sendPasswordReset(email: String)
    suspend fun signOut()
}
