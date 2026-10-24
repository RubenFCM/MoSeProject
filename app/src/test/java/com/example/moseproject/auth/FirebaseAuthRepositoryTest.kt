package com.example.moseproject.auth

import com.example.moseproject.data.auth.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Test
import java.util.concurrent.Executor
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthRepositoryTest {
    private val auth = mockk<FirebaseAuth>(relaxed = true)
    private val user = mockk<FirebaseUser> {
        every { uid } returns "test-user"
        every { email } returns "viewer@example.test"
        every { isEmailVerified } returns true
        every { reload() } returns completedTask<Void?>(null)
    }

    @Test fun `verified flag alone is insufficient until forced fresh token has verified claims`() = runTest {
        every { auth.currentUser } returns user
        val token = mockk<GetTokenResult> { every { claims } returns mapOf("email_verified" to false) }
        every { user.getIdToken(true) } returns completedTask(token)
        val repo = FirebaseAuthRepository(auth)
        assertIs<AuthSession.PendingVerification>(repo.refreshSession())
        every { token.claims } returns mapOf("email_verified" to true)
        assertIs<AuthSession.Authenticated>(repo.refreshSession())
        verify(exactly = 2) { user.reload(); user.getIdToken(true) }
        verify(exactly = 0) { token.token }
    }

    @Test fun `deleted or disabled user invalidates the SDK session`() = runTest {
        every { auth.currentUser } returns user
        every { user.reload() } returns failedTask(mockk<FirebaseAuthInvalidUserException>(relaxed = true))
        val error = assertFailsWith<AuthException> { FirebaseAuthRepository(auth).refreshSession() }
        assertEquals(AuthFailure.SessionExpired, error.failure)
        verify(exactly = 1) { auth.signOut() }
    }

    @Test fun `password reset for an unknown account has the same successful outcome`() = runTest {
        every { auth.sendPasswordResetEmail(any()) } returns failedTask(mockk<FirebaseAuthInvalidUserException>(relaxed = true))
        FirebaseAuthRepository(auth).sendPasswordReset("unknown@example.test")
        verify(exactly = 0) { auth.signOut() }
    }

    @Test fun `session listener is removed when collection ends`() = runTest {
        every { auth.currentUser } returns null
        every { auth.addIdTokenListener(any<FirebaseAuth.IdTokenListener>()) } answers { firstArg<FirebaseAuth.IdTokenListener>().onIdTokenChanged(auth) }
        assertNull(FirebaseAuthRepository(auth).users.first())
        verify(exactly = 1) { auth.removeIdTokenListener(any<FirebaseAuth.IdTokenListener>()) }
    }

    @Test fun `cancelled sign in finishes before a queued sign out so a late Task cannot restore the session`() = runTest {
        var finished = false
        val completion = slot<OnCompleteListener<AuthResult>>()
        val result = mockk<AuthResult>()
        val task = mockk<Task<AuthResult>>(relaxed = true) {
            every { isComplete } answers { finished }
            every { isCanceled } returns false
            every { exception } returns null
            every { this@mockk.result } returns result
            every { addOnCompleteListener(any<Executor>(), capture(completion)) } answers { this@mockk }
        }
        every { auth.signInWithEmailAndPassword(any(), any()) } returns task
        val repo = FirebaseAuthRepository(auth)
        val signingIn = launch { repo.signIn("viewer@example.test", "test password") }
        runCurrent()
        signingIn.cancel()
        val signingOut = launch { repo.signOut() }
        runCurrent()
        verify(exactly = 0) { auth.signOut() }
        finished = true
        completion.captured.onComplete(task)
        runCurrent()
        assertTrue(signingIn.isCancelled)
        assertTrue(signingOut.isCompleted)
        verify(exactly = 1) { auth.signOut() }
    }
}

private fun <T> completedTask(value: T): Task<T> = mockk(relaxed = true) {
    every { isComplete } returns true
    every { isCanceled } returns false
    every { exception } returns null
    every { result } returns value
}

private fun <T> failedTask(error: Exception): Task<T> = mockk(relaxed = true) {
    every { isComplete } returns true
    every { isCanceled } returns false
    every { exception } returns error
}
