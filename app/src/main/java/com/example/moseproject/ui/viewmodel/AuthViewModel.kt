package com.example.moseproject.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.auth.AuthException
import com.example.moseproject.data.auth.AuthFailure
import com.example.moseproject.data.auth.AuthRepository
import com.example.moseproject.data.auth.AuthSession
import com.example.moseproject.data.auth.EmailValidation
import com.example.moseproject.data.auth.PasswordPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthOperation { SignIn, Register, ResetPassword, ResendVerification, CheckVerification, SignOut }
enum class AuthField { Email, Password, Confirmation }
enum class AuthValidation { Email, PasswordRequired, PasswordPolicy, PasswordMismatch }
enum class AuthNotice { VerificationSent, NotVerifiedYet, PasswordResetSent }

data class AuthUiState(
    val session: AuthSession = AuthSession.Initializing,
    val operation: AuthOperation? = null,
    val failure: AuthFailure? = null,
    val fieldErrors: Map<AuthField, AuthValidation> = emptyMap(),
    val notice: AuthNotice? = null,
    val resendSeconds: Int = 0,
    val resetSeconds: Int = 0,
    val checkSeconds: Int = 0
)

class AuthViewModel(
    private val repository: AuthRepository,
    val passwordPolicy: PasswordPolicy,
    private val now: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null
    private var ticker: Job? = null
    private var resendUntil = 0L
    private var resetUntil = 0L
    private var checkUntil = 0L

    init { observeSession() }

    private fun observeSession() {
        observer?.cancel()
        mutableState.update { it.copy(session = AuthSession.Initializing, failure = null) }
        observer = viewModelScope.launch {
            try {
                repository.users.collectLatest { user ->
                    if (user == null) {
                        clearUserState()
                    } else {
                        // Cached SDK users never directly grant entry to the account screen.
                        mutableState.update { it.copy(session = AuthSession.Initializing) }
                        try {
                            val session = repository.refreshSession()
                            mutableState.update { it.copy(session = session) }
                        } catch (error: AuthException) {
                            mutableState.update { it.copy(session = AuthSession.Unavailable(error.failure)) }
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthException) {
                mutableState.update { it.copy(session = AuthSession.Unavailable(error.failure)) }
            } catch (_: Exception) {
                mutableState.update { it.copy(session = AuthSession.Unavailable(AuthFailure.Unknown)) }
            }
        }
    }

    fun retrySession() {
        if (state.value.operation == null) observeSession()
    }

    fun clearFeedback() {
        mutableState.update { it.copy(fieldErrors = emptyMap(), failure = null, notice = null) }
    }

    fun signIn(email: String, password: String) {
        if (!validate(email, password)) return
        execute(AuthOperation.SignIn) { repository.signIn(email.trim(), password) }
    }

    fun register(email: String, password: String, confirmation: String) {
        if (!validate(email, password, confirmation)) return
        execute(AuthOperation.Register) {
            repository.register(email.trim(), password)
            reserveVerificationSend()
            repository.sendVerification()
            mutableState.update { it.copy(notice = AuthNotice.VerificationSent) }
        }
    }

    fun resetPassword(email: String) {
        if (now() < resetUntil || !validate(email)) return
        execute(AuthOperation.ResetPassword) {
            resetUntil = now() + 60_000
            startTicker()
            repository.sendPasswordReset(email.trim())
            mutableState.update { it.copy(notice = AuthNotice.PasswordResetSent) }
        }
    }

    fun resendVerification() {
        if (state.value.session !is AuthSession.PendingVerification || now() < resendUntil) return
        execute(AuthOperation.ResendVerification) {
            reserveVerificationSend()
            repository.sendVerification()
            mutableState.update { it.copy(notice = AuthNotice.VerificationSent) }
        }
    }

    fun checkVerification() {
        if (state.value.session !is AuthSession.PendingVerification || now() < checkUntil) return
        execute(AuthOperation.CheckVerification) {
            checkUntil = now() + 5_000
            startTicker()
            // Keep the pending screen visible while checking; it contains no private content.
            val session = repository.refreshSession()
            mutableState.update { it.copy(session = session,
                notice = if (session is AuthSession.PendingVerification) AuthNotice.NotVerifiedYet else null) }
        }
    }

    fun signOut() = execute(AuthOperation.SignOut) {
        repository.signOut()
        // Also clear synchronously; rendering must not wait for another SDK callback.
        clearUserState()
    }

    private fun clearUserState() {
        resendUntil = 0
        resetUntil = 0
        checkUntil = 0
        ticker?.cancel()
        mutableState.update { AuthUiState(session = AuthSession.SignedOut, operation = it.operation) }
    }

    private fun validate(email: String, password: String? = null, confirmation: String? = null): Boolean {
        if (state.value.operation != null) return false
        val errors = buildMap {
            if (!EmailValidation.accepts(email.trim())) put(AuthField.Email, AuthValidation.Email)
            if (password != null && password.isEmpty()) put(AuthField.Password, AuthValidation.PasswordRequired)
            if (confirmation != null) {
                if (password == null || !passwordPolicy.accepts(password)) put(AuthField.Password, AuthValidation.PasswordPolicy)
                if (password != confirmation) put(AuthField.Confirmation, AuthValidation.PasswordMismatch)
            }
        }
        mutableState.update { it.copy(fieldErrors = errors, failure = null, notice = null) }
        return errors.isEmpty()
    }

    private fun execute(operation: AuthOperation, action: suspend () -> Unit) {
        if (state.value.operation != null) return
        // Set before launching so two taps in the same frame cannot submit twice.
        mutableState.update { it.copy(operation = operation, failure = null, notice = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthException) {
                // Registration/login never expose collision, unknown-user, or disabled-user details.
                val failure = when {
                    operation == AuthOperation.SignIn && error.failure in setOf(AuthFailure.Credentials, AuthFailure.SessionExpired, AuthFailure.Registration) -> AuthFailure.Credentials
                    operation == AuthOperation.Register && error.failure in setOf(AuthFailure.Credentials, AuthFailure.SessionExpired, AuthFailure.Registration) -> AuthFailure.Registration
                    else -> error.failure
                }
                mutableState.update { it.copy(failure = failure) }
            } catch (_: Exception) {
                mutableState.update { it.copy(failure = AuthFailure.Unknown) }
            } finally {
                mutableState.update { it.copy(operation = null) }
            }
        }
    }

    private fun reserveVerificationSend() {
        resendUntil = now() + 60_000
        startTicker()
    }

    private fun startTicker() {
        ticker?.cancel()
        updateCountdowns()
        ticker = viewModelScope.launch {
            while (maxOf(resendUntil, resetUntil, checkUntil) > now()) {
                delay(1_000)
                updateCountdowns()
            }
        }
    }

    private fun updateCountdowns() {
        fun seconds(until: Long) = ((until - now()).coerceAtLeast(0) + 999).div(1_000).toInt()
        mutableState.update { it.copy(resendSeconds = seconds(resendUntil), resetSeconds = seconds(resetUntil), checkSeconds = seconds(checkUntil)) }
    }

    companion object {
        fun factory(repository: AuthRepository, passwordPolicy: PasswordPolicy): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(AuthViewModel::class.java))
                    @Suppress("UNCHECKED_CAST") // Factory contract; checked against modelClass above.
                    return AuthViewModel(repository, passwordPolicy) as T
                }
            }
    }
}
