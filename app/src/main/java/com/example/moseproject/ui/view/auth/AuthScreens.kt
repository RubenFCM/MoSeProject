package com.example.moseproject.ui.view.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moseproject.R
import com.example.moseproject.data.auth.AuthFailure
import com.example.moseproject.data.auth.AuthSession
import com.example.moseproject.data.auth.PasswordPolicy
import com.example.moseproject.ui.theme.CinemaAccent
import com.example.moseproject.ui.theme.CinemaBackground
import com.example.moseproject.ui.theme.CinemaMuted
import com.example.moseproject.ui.theme.CinemaSurface
import com.example.moseproject.ui.theme.CinemaText
import com.example.moseproject.ui.viewmodel.*

/** An isolated, UID-scoped account navigation tree. Disposed on logout or account changes. */
@Composable
fun AccountRoute(viewModel: AuthViewModel, onBrowse: () -> Unit) {
    val window = (LocalContext.current as Activity).window
    val view = LocalView.current
    DisposableEffect(window, view) {
        // The existing app theme is fullscreen; adjustResize is ignored there on older Android.
        // Scope this window change to account forms so the keyboard does not hide the actions.
        val statusBars = WindowInsetsCompat.Type.statusBars()
        val wasVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(statusBars) ?: true
        val controller = WindowCompat.getInsetsController(window, view)
        controller.show(statusBars)
        onDispose { if (!wasVisible) controller.hide(statusBars) }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session = state.session
    val scopeKey = when (session) {
        is AuthSession.Authenticated -> "verified:${session.user.uid}"
        is AuthSession.PendingVerification -> "pending:${session.user.uid}"
        else -> session.javaClass.name
    }
    key(scopeKey) {
        when (session) {
            AuthSession.SignedOut -> {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "login") {
                    composable("login") {
                        AuthForm(AuthPage.Login, state, viewModel, onBrowse,
                            onLogin = { nav.popBackStack("login", false) },
                            onRegister = { nav.navigate("register") { launchSingleTop = true } },
                            onReset = { nav.navigate("reset") { launchSingleTop = true } })
                    }
                    composable("register") {
                        AuthForm(AuthPage.Register, state, viewModel, onBrowse,
                            onLogin = { nav.popBackStack("login", false) }, onRegister = {}, onReset = {})
                    }
                    composable("reset") {
                        AuthForm(AuthPage.Reset, state, viewModel, onBrowse,
                            onLogin = { nav.popBackStack("login", false) }, onRegister = {}, onReset = {})
                    }
                }
            }
            AuthSession.Initializing -> AuthPageLayout(R.string.auth_account, onBrowse) {
                CircularProgressIndicator(color = CinemaAccent)
                Text(stringResource(R.string.auth_initializing))
            }
            is AuthSession.Unavailable -> AuthPageLayout(R.string.auth_unavailable_title, onBrowse) {
                Feedback(stringResource(failureResource(session.reason)), error = true)
                if (session.reason != AuthFailure.Configuration) {
                    Button(onClick = viewModel::retrySession, enabled = state.operation == null) { Text(stringResource(R.string.auth_retry)) }
                    TextButton(onClick = viewModel::signOut, enabled = state.operation == null) { Text(stringResource(R.string.auth_return_login)) }
                }
            }
            is AuthSession.PendingVerification -> AuthPageLayout(R.string.auth_verification_title, onBrowse) {
                Text(stringResource(R.string.auth_verification_description, session.user.email), color = CinemaMuted)
                StatusFeedback(state)
                ActionButton(R.string.auth_check_verification, state, state.checkSeconds, viewModel::checkVerification)
                ActionButton(R.string.auth_resend, state, state.resendSeconds, viewModel::resendVerification)
                TextButton(onClick = viewModel::signOut, enabled = state.operation == null) { Text(stringResource(R.string.auth_sign_out)) }
            }
            is AuthSession.Authenticated -> AuthPageLayout(R.string.auth_verified_title, onBrowse) {
                Text(session.user.email, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.auth_verified_description), color = CinemaMuted)
                StatusFeedback(state)
                ActionButton(R.string.auth_sign_out, state, 0, viewModel::signOut)
            }
        }
    }
}

private enum class AuthPage { Login, Register, Reset }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthForm(
    page: AuthPage, state: AuthUiState, viewModel: AuthViewModel, onBrowse: () -> Unit,
    onLogin: () -> Unit, onRegister: () -> Unit, onReset: () -> Unit
) {
    // Deliberately not rememberSaveable/SavedStateHandle: no credentials in saved instance state.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { password = ""; confirmation = "" }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(page) { viewModel.clearFeedback() }
    val keyboard = LocalSoftwareKeyboardController.current
    val busy = state.operation != null
    val submit: () -> Unit = {
        keyboard?.hide()
        when (page) {
            AuthPage.Login -> viewModel.signIn(email, password)
            AuthPage.Register -> viewModel.register(email, password, confirmation)
            AuthPage.Reset -> viewModel.resetPassword(email)
        }
        if (viewModel.state.value.operation != null) { password = ""; confirmation = "" }
    }
    val title = when (page) { AuthPage.Login -> R.string.auth_login_title; AuthPage.Register -> R.string.auth_register_title; AuthPage.Reset -> R.string.auth_reset_title }
    AuthPageLayout(title, if (page == AuthPage.Login) onBrowse else onLogin) {
        Text(stringResource(when (page) { AuthPage.Login -> R.string.auth_login_description; AuthPage.Register -> R.string.auth_register_description; AuthPage.Reset -> R.string.auth_reset_description }), color = CinemaMuted)
        CredentialField(email, { email = it }, R.string.auth_email, !busy,
            state.fieldErrors[AuthField.Email], emailField = true, last = page == AuthPage.Reset, onSubmit = submit)
        if (page != AuthPage.Reset) {
            CredentialField(password, { password = it }, R.string.auth_password, !busy,
                state.fieldErrors[AuthField.Password], newPassword = page == AuthPage.Register,
                last = page == AuthPage.Login, onSubmit = submit)
        }
        if (page == AuthPage.Register) {
            PasswordRequirements(viewModel.passwordPolicy)
            CredentialField(confirmation, { confirmation = it }, R.string.auth_confirmation, !busy,
                state.fieldErrors[AuthField.Confirmation], newPassword = true, last = true, onSubmit = submit)
        }
        StatusFeedback(state)
        ActionButton(when (page) { AuthPage.Login -> R.string.auth_sign_in; AuthPage.Register -> R.string.auth_create_account; AuthPage.Reset -> R.string.auth_reset_action },
            state, if (page == AuthPage.Reset) state.resetSeconds else 0, submit)
        if (page == AuthPage.Login) {
            TextButton(onClick = onReset, enabled = !busy) { Text(stringResource(R.string.auth_forgot_password)) }
            OutlinedButton(onClick = onRegister, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.auth_create_account)) }
        } else {
            TextButton(onClick = onLogin, enabled = !busy) { Text(stringResource(R.string.auth_have_account)) }
        }
        TextButton(onClick = onBrowse) { Text(stringResource(R.string.auth_browse)) }
    }
}

@OptIn(ExperimentalLayoutApi::class) // Insets consumption is experimental in the current release Compose BOM.
@Composable
private fun AuthPageLayout(title: Int, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(containerColor = CinemaBackground, contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.auth_back)) }
                Text(stringResource(R.string.auth_account), style = MaterialTheme.typography.titleMedium)
            }
        }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = CinemaSurface, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.padding(16.dp).size(32.dp), tint = CinemaAccent)
                }
                Text(stringResource(R.string.auth_brand), style = MaterialTheme.typography.labelSmall, color = CinemaAccent)
                Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, color = CinemaText)
                content()
            }
        }
    }
}

/** The explicit AutofillNode bridge supports the project's Compose 1.5 UI without a UI upgrade. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CredentialField(
    value: String, onChange: (String) -> Unit, label: Int, enabled: Boolean, error: AuthValidation?,
    emailField: Boolean = false, newPassword: Boolean = false, last: Boolean = false, onSubmit: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val autofill = LocalAutofill.current
    val tree = LocalAutofillTree.current
    val change by rememberUpdatedState(onChange)
    val node = remember(emailField, newPassword) {
        AutofillNode(listOf(when { emailField -> AutofillType.EmailAddress; newPassword -> AutofillType.NewPassword; else -> AutofillType.Password }), onFill = { change(it) })
    }
    DisposableEffect(tree, node) {
        tree += node
        onDispose { autofill?.cancelAutofillForNode(node); tree.children.remove(node.id) }
    }
    val focus = LocalFocusManager.current
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth().testTag(when (label) {
            R.string.auth_email -> "auth_email"
            R.string.auth_confirmation -> "auth_confirmation"
            else -> "auth_password"
        }).onGloballyPositioned { node.boundingBox = it.boundsInWindow() }
            .onFocusChanged { if (it.isFocused && enabled) autofill?.requestAutofillForNode(node) else autofill?.cancelAutofillForNode(node) },
        enabled = enabled, singleLine = true, isError = error != null,
        supportingText = if (error == null) null else ({ Text(stringResource(validationResource(error))) }),
        visualTransformation = if (emailField || visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (emailField) null else ({ TextButton(onClick = { visible = !visible }, enabled = enabled) {
            Text(stringResource(if (visible) R.string.auth_hide_password else R.string.auth_show_password))
        } }),
        keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = if (emailField) KeyboardType.Email else KeyboardType.Password,
            imeAction = if (last) ImeAction.Done else ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }, onDone = { if (enabled) onSubmit() }))
}

@Composable
private fun PasswordRequirements(policy: PasswordPolicy) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.auth_password_length, policy.minLength, policy.maxLength), style = MaterialTheme.typography.bodySmall, color = CinemaMuted)
        listOf(policy.lowercase to R.string.auth_password_lowercase, policy.uppercase to R.string.auth_password_uppercase,
            policy.number to R.string.auth_password_number, policy.symbol to R.string.auth_password_symbol)
            .filter { it.first }.forEach { (_, resource) -> Text(stringResource(resource), style = MaterialTheme.typography.bodySmall, color = CinemaMuted) }
    }
}

@Composable
private fun ActionButton(label: Int, state: AuthUiState, seconds: Int, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = state.operation == null && seconds == 0, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        if (state.operation != null) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(stringResource(if (state.operation != null) R.string.auth_working else if (seconds > 0) R.string.auth_wait_seconds else label,
            *if (seconds > 0 && state.operation == null) arrayOf<Any>(seconds) else emptyArray()))
    }
}

@Composable
private fun StatusFeedback(state: AuthUiState) {
    state.failure?.let { Feedback(stringResource(failureResource(it)), error = true) }
    state.notice?.let { Feedback(stringResource(when (it) {
        AuthNotice.VerificationSent -> R.string.auth_verification_sent
        AuthNotice.NotVerifiedYet -> R.string.auth_not_verified
        AuthNotice.PasswordResetSent -> R.string.auth_reset_sent
    }), error = false) }
}

@Composable
private fun Feedback(message: String, error: Boolean) {
    Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = if (error) MaterialTheme.colorScheme.error else CinemaAccent, style = MaterialTheme.typography.bodyMedium)
}

private fun validationResource(error: AuthValidation): Int = when (error) {
    AuthValidation.Email -> R.string.auth_email_invalid
    AuthValidation.PasswordRequired -> R.string.auth_password_required
    AuthValidation.PasswordMismatch -> R.string.auth_password_mismatch
    AuthValidation.PasswordPolicy -> R.string.auth_policy_error
}

private fun failureResource(error: AuthFailure): Int = when (error) {
    AuthFailure.Configuration -> R.string.auth_configuration_error
    AuthFailure.Network -> R.string.auth_network_error
    AuthFailure.Credentials -> R.string.auth_credentials_error
    AuthFailure.Registration -> R.string.auth_registration_error
    AuthFailure.PasswordPolicy -> R.string.auth_policy_error
    AuthFailure.RateLimited -> R.string.auth_rate_error
    AuthFailure.SessionExpired -> R.string.auth_session_error
    AuthFailure.Unknown -> R.string.auth_unknown_error
}
