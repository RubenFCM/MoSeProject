package com.example.moseproject.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import com.example.moseproject.R
import com.example.moseproject.data.auth.*
import com.example.moseproject.ui.theme.MoSeProjectTheme
import com.example.moseproject.ui.view.auth.AccountRoute
import com.example.moseproject.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI only: this repository never initializes Firebase or sends email. */
class AuthScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val repository = ScreenAuthRepository()
    private lateinit var viewModel: AuthViewModel

    @Before fun setUp() {
        compose.activityRule.scenario.onActivity { activity ->
            viewModel = AuthViewModel(repository, PasswordPolicy())
            activity.viewModelStore.put("auth-test", viewModel)
        }
    }

    private fun showAccount() {
        compose.setContent { MoSeProjectTheme { AccountRoute(viewModel, onBrowse = {}) } }
    }

    private fun text(resource: Int) = compose.activity.getString(resource)

    @Test fun registrationShowsConfirmationValidationAndPasswordVisibility() {
        showAccount()
        compose.onNodeWithText(text(R.string.auth_create_account)).performScrollTo().performClick()
        compose.onNodeWithTag("auth_email").performTextInput("viewer@example.test")
        compose.onNodeWithTag("auth_password").performTextInput("some password")
        compose.onNodeWithTag("auth_confirmation").performTextInput("different password")
        compose.onAllNodesWithText(text(R.string.auth_show_password))[0].performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.auth_hide_password)).assertExists()
        compose.onNodeWithText(text(R.string.auth_create_account)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.auth_password_mismatch)).assertExists()
        compose.runOnIdle { check(repository.registerCalls == 0) }
    }

    @Test fun restoringSessionNeverFlashesAnAuthenticatedAccountAndLogoutDropsItsContents() {
        repository.refreshGate = CompletableDeferred()
        repository.current.value = AuthUser("test-user", "viewer@example.test", true)
        showAccount()
        compose.onNodeWithText(text(R.string.auth_initializing)).assertExists()
        compose.onNodeWithText("viewer@example.test").assertDoesNotExist()
        compose.runOnIdle { repository.refreshGate!!.complete(AuthSession.Authenticated(repository.current.value!!)) }
        compose.onNodeWithText("viewer@example.test").assertExists()
        compose.onNodeWithText(text(R.string.auth_sign_out)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.auth_login_title)).assertExists()
        compose.onNodeWithText("viewer@example.test").assertDoesNotExist()
        compose.onNodeWithText(text(R.string.auth_have_account)).assertDoesNotExist()
    }

    @Test fun passwordIsNotRestoredAfterGoingToBackground() {
        showAccount()
        compose.onNodeWithTag("auth_password").performTextInput("temporary test password")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithTag("auth_password").assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
    }
}

private class ScreenAuthRepository : AuthRepository {
    val current = MutableStateFlow<AuthUser?>(null)
    override val users = current
    var refreshGate: CompletableDeferred<AuthSession>? = null
    var registerCalls = 0
    override suspend fun refreshSession(): AuthSession = refreshGate?.await() ?: current.value?.let {
        if (it.emailVerified) AuthSession.Authenticated(it) else AuthSession.PendingVerification(it)
    } ?: AuthSession.SignedOut
    override suspend fun signIn(email: String, password: String) { current.value = AuthUser("test-user", email, true) }
    override suspend fun register(email: String, password: String) { registerCalls++; current.value = AuthUser("test-user", email, false) }
    override suspend fun sendVerification() = Unit
    override suspend fun sendPasswordReset(email: String) = Unit
    override suspend fun signOut() { current.value = null }
}
