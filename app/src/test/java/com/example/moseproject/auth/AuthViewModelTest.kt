package com.example.moseproject.auth

import androidx.lifecycle.ViewModelStore
import com.example.moseproject.data.auth.*
import com.example.moseproject.ui.viewmodel.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val user = AuthUser("test-user", "viewer@example.test", false)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun model(repo: FakeAuthRepository, policy: PasswordPolicy = PasswordPolicy()): AuthViewModel =
        AuthViewModel(repo, policy) { dispatcher.scheduler.currentTime }.also { store.put("auth", it) }

    @Test fun `restored cached verified user cannot open account before server and token validation`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user.copy(emailVerified = true))
        repo.refreshGate = CompletableDeferred()
        val vm = model(repo)
        runCurrent()
        assertEquals(AuthSession.Initializing, vm.state.value.session)
        repo.refreshGate!!.complete(AuthSession.Authenticated(user.copy(emailVerified = true)))
        runCurrent()
        assertIs<AuthSession.Authenticated>(vm.state.value.session)
    }

    @Test fun `missing configuration and network failures never grant account access and can be retried`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user)
        repo.refreshFailure = AuthFailure.Network
        val vm = model(repo)
        runCurrent()
        assertEquals(AuthSession.Unavailable(AuthFailure.Network), vm.state.value.session)
        repo.refreshFailure = null
        vm.retrySession()
        runCurrent()
        assertIs<AuthSession.PendingVerification>(vm.state.value.session)
        repo.refreshFailure = AuthFailure.Configuration
        vm.retrySession()
        runCurrent()
        assertEquals(AuthSession.Unavailable(AuthFailure.Configuration), vm.state.value.session)
    }

    @Test fun `registration validates email policy and exact confirmation without contacting Firebase`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val vm = model(repo, PasswordPolicy(minLength = 8, uppercase = true, number = true, symbol = true))
        runCurrent()
        vm.register("invalid", "weak", "different")
        assertEquals(0, repo.registerCalls)
        assertEquals(setOf(AuthField.Email, AuthField.Password, AuthField.Confirmation), vm.state.value.fieldErrors.keys)
    }

    @Test fun `password whitespace is preserved and duplicate submissions are rejected`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        repo.signInGate = CompletableDeferred()
        val vm = model(repo)
        runCurrent()
        vm.signIn(" viewer@example.test ", " secret with spaces ")
        vm.signIn("viewer@example.test", "second password")
        runCurrent()
        assertEquals(1, repo.signInCalls)
        assertEquals("viewer@example.test", repo.lastEmail)
        assertEquals(" secret with spaces ", repo.lastPassword)
        repo.signInGate!!.complete(Unit)
        runCurrent()
        assertNull(vm.state.value.operation)
    }

    @Test fun `successful registration stays pending and sends verification once with a cooldown`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val vm = model(repo)
        runCurrent()
        vm.register(user.email, " secret ", " secret ")
        runCurrent()
        assertEquals(" secret ", repo.lastPassword)
        assertIs<AuthSession.PendingVerification>(vm.state.value.session)
        assertEquals(1, repo.verificationCalls)
        assertEquals(AuthNotice.VerificationSent, vm.state.value.notice)
        vm.resendVerification()
        runCurrent()
        assertEquals(1, repo.verificationCalls)
        advanceTimeBy(60_000)
        runCurrent()
        vm.resendVerification()
        runCurrent()
        assertEquals(2, repo.verificationCalls)
    }

    @Test fun `failed verification delivery leaves the created account pending and rate limits retries`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        repo.verificationFailure = AuthFailure.Network
        val vm = model(repo)
        runCurrent()
        vm.register(user.email, "secret", "secret")
        runCurrent()
        assertIs<AuthSession.PendingVerification>(vm.state.value.session)
        assertEquals(AuthFailure.Network, vm.state.value.failure)
        assertEquals(60, vm.state.value.resendSeconds)
        vm.resendVerification()
        runCurrent()
        assertEquals(1, repo.verificationCalls)
    }

    @Test fun `explicit verification check refreshes session and only unlocks after server confirmation`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user)
        val vm = model(repo)
        runCurrent()
        vm.checkVerification()
        runCurrent()
        assertEquals(AuthNotice.NotVerifiedYet, vm.state.value.notice)
        val requests = repo.refreshCalls
        vm.checkVerification()
        runCurrent()
        assertEquals(requests, repo.refreshCalls)
        advanceTimeBy(5_000)
        runCurrent()
        repo.refreshedSession = AuthSession.Authenticated(user.copy(emailVerified = true))
        vm.checkVerification()
        runCurrent()
        assertIs<AuthSession.Authenticated>(vm.state.value.session)
        assertNull(vm.state.value.notice)
    }

    @Test fun `login errors do not disclose whether a user is missing disabled or has a wrong password`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val vm = model(repo)
        runCurrent()
        for (failure in listOf(AuthFailure.Credentials, AuthFailure.SessionExpired, AuthFailure.Registration)) {
            repo.signInFailure = failure
            vm.signIn(user.email, "some password")
            runCurrent()
            assertEquals(AuthFailure.Credentials, vm.state.value.failure)
            assertNull(vm.state.value.operation)
        }
    }

    @Test fun `password reset has a neutral response and repeated requests are controlled`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val vm = model(repo)
        runCurrent()
        vm.resetPassword(user.email)
        runCurrent()
        assertEquals(AuthNotice.PasswordResetSent, vm.state.value.notice)
        vm.resetPassword("another@example.test")
        runCurrent()
        assertEquals(1, repo.resetCalls)
        assertEquals(60, vm.state.value.resetSeconds)
    }

    @Test fun `logout clears identity feedback cooldowns and subsequent accounts cannot inherit user state`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user)
        val vm = model(repo)
        runCurrent()
        vm.resendVerification()
        runCurrent()
        assertTrue(vm.state.value.resendSeconds > 0)
        vm.signOut()
        runCurrent()
        assertEquals(1, repo.signOutCalls)
        assertEquals(AuthUiState(session = AuthSession.SignedOut), vm.state.value)
        repo.current.value = user.copy(uid = "another-user", email = "other@example.test", emailVerified = true)
        runCurrent()
        val session = assertIs<AuthSession.Authenticated>(vm.state.value.session)
        assertEquals("other@example.test", session.user.email)
        assertNull(vm.state.value.notice)
        assertEquals(0, vm.state.value.resendSeconds)
    }

    @Test fun `external session loss immediately removes private identity`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user.copy(emailVerified = true))
        val vm = model(repo)
        runCurrent()
        assertIs<AuthSession.Authenticated>(vm.state.value.session)
        repo.current.value = null
        runCurrent()
        assertEquals(AuthSession.SignedOut, vm.state.value.session)
    }

    @Test fun `clearing viewmodel releases session subscriptions and timers`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(user)
        val vm = model(repo)
        runCurrent()
        assertEquals(1, repo.subscriptions)
        vm.resendVerification()
        runCurrent()
        store.clear()
        runCurrent()
        assertEquals(0, repo.subscriptions)
    }
}

private class FakeAuthRepository(initial: AuthUser? = null) : AuthRepository {
    val current = MutableStateFlow(initial)
    var subscriptions = 0
    override val users = current.onStart { subscriptions++ }.onCompletion { subscriptions-- }
    var refreshGate: CompletableDeferred<AuthSession>? = null
    var signInGate: CompletableDeferred<Unit>? = null
    var refreshedSession: AuthSession? = null
    var refreshFailure: AuthFailure? = null
    var signInFailure: AuthFailure? = null
    var verificationFailure: AuthFailure? = null
    var signInCalls = 0
    var registerCalls = 0
    var verificationCalls = 0
    var refreshCalls = 0
    var resetCalls = 0
    var signOutCalls = 0
    var lastEmail: String? = null
    var lastPassword: String? = null

    override suspend fun refreshSession(): AuthSession {
        refreshCalls++
        refreshFailure?.let { throw AuthException(it) }
        refreshGate?.let { return it.await() }
        refreshedSession?.let { return it }
        val user = current.value ?: return AuthSession.SignedOut
        return if (user.emailVerified) AuthSession.Authenticated(user) else AuthSession.PendingVerification(user)
    }
    override suspend fun signIn(email: String, password: String) {
        signInCalls++; lastEmail = email; lastPassword = password
        signInGate?.await()
        signInFailure?.let { throw AuthException(it) }
        current.value = AuthUser("test-user", email, true)
    }
    override suspend fun register(email: String, password: String) {
        registerCalls++; lastEmail = email; lastPassword = password
        current.value = AuthUser("test-user", email, false)
    }
    override suspend fun sendVerification() { verificationCalls++; verificationFailure?.let { throw AuthException(it) } }
    override suspend fun sendPasswordReset(email: String) { resetCalls++ }
    override suspend fun signOut() { signOutCalls++; current.value = null; refreshedSession = null }
}
