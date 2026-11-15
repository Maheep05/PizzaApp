package com.pizza5ps.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.data.AccountRepository
import com.pizza5ps.app.ui.components.PrimaryActionButton
import com.pizza5ps.app.ui.components.TagPill
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.viewmodel.PizzaViewModel

/** Sign in or create an account — the same form, two modes. */
private enum class AuthMode(val title: String, val cta: String, val switchPrompt: String, val switchAction: String) {
    SIGN_IN("Welcome back", "Sign in", "New to Pizza 5 P's?", "Create an account"),
    SIGN_UP("Create your account", "Create account", "Already have an account?", "Sign in")
}

/**
 * Sign-in and sign-up.
 *
 * One screen with two modes rather than two screens: the fields overlap almost
 * entirely, and switching between them is the single most common thing people do
 * here — a mode toggle keeps that instant instead of a navigation.
 *
 * Authentication is a local in-memory stand-in (see
 * [com.pizza5ps.app.data.AccountRepository]); the screen exists so the greeting,
 * profile and saved addresses have a real customer to belong to.
 */
@Composable
fun AuthScreen(
    viewModel: PizzaViewModel,
    onAuthenticated: () -> Unit
) {
    val account by viewModel.account.collectAsState()
    val error by viewModel.authError.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Leaving is driven by the account appearing, not by the button press, so the
    // screen can never navigate away on a failed attempt.
    LaunchedEffect(account) {
        if (account != null) onAuthenticated()
    }

    val submit = {
        when (mode) {
            AuthMode.SIGN_IN -> viewModel.signIn(email, password)
            AuthMode.SIGN_UP -> viewModel.signUp(name, email, password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PizzaTheme.colors.ground)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PizzaTheme.colors.brandSoft,
                        PizzaTheme.colors.ground.copy(alpha = 0f)
                    ),
                    center = Offset(140f, 120f),
                    radius = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Space.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Space.huge))

            Brandmark()

            Spacer(modifier = Modifier.height(Space.xxl))

            // The heading changes with the mode, so it slides rather than swapping.
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    (slideInVertically(Motion.mediumTween()) { it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically(Motion.mediumTween()) { -it / 2 } + fadeOut())
                },
                label = "authHeading"
            ) { current ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = current.title,
                        fontFamily = PlayfairFamily,
                        style = MaterialTheme.typography.displayMedium,
                        color = PizzaTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Perfect Pizza. Perfect Moments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PizzaTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.xxl))

            if (mode == AuthMode.SIGN_UP) {
                AuthField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.clearAuthError()
                    },
                    label = "Full name",
                    leadingIcon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(Space.md))
            }

            AuthField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearAuthError()
                },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(Space.md))

            AuthField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearAuthError()
                },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                imeAction = ImeAction.Done,
                onDone = submit
            )

            // Errors take a slot only when there is one, so the form does not jump.
            if (error != null) {
                Spacer(modifier = Modifier.height(Space.md))
                Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PizzaTheme.colors.brand,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Space.xl))

            PrimaryActionButton(
                label = mode.cta,
                onClick = submit
            )

            Spacer(modifier = Modifier.height(Space.lg))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Text(
                    text = mode.switchPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PizzaTheme.colors.textMuted
                )
                Text(
                    text = mode.switchAction,
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.brand,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .bouncyClick(pressedScale = 0.94f) {
                            mode = if (mode == AuthMode.SIGN_IN) {
                                AuthMode.SIGN_UP
                            } else {
                                AuthMode.SIGN_IN
                            }
                            viewModel.clearAuthError()
                        }
                        .padding(horizontal = Space.xs, vertical = Space.xxs)
                )
            }

            Spacer(modifier = Modifier.height(Space.xl))

            // Prefilled demo credentials, because a marker should not have to create
            // an account before they can look at the app.
            if (mode == AuthMode.SIGN_IN) {
                DemoCredentialHint(
                    onUse = {
                        val (demoEmail, demoPassword) = AccountRepository.demoCredentials
                        email = demoEmail
                        password = demoPassword
                        viewModel.clearAuthError()
                    }
                )
            }

            Spacer(modifier = Modifier.height(Space.huge))
        }
    }
}

@Composable
private fun Brandmark(modifier: Modifier = Modifier) {
    val pop = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, Motion.bouncySpring())
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
                alpha = pop.value.coerceIn(0f, 1f)
            }
            .clip(CircleShape)
            .background(PizzaTheme.colors.surface),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                id = com.pizza5ps.app.R.drawable.ic_pizza_logo
            ),
            contentDescription = "Pizza 5 P's",
            modifier = Modifier.size(78.dp)
        )
    }
}

@Composable
private fun DemoCredentialHint(
    onUse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (demoEmail, _) = AccountRepository.demoCredentials
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Demo account",
            style = MaterialTheme.typography.labelSmall,
            color = PizzaTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(Space.sm))
        Box(
            modifier = Modifier.bouncyClick(pressedScale = 0.94f, onClick = onUse)
        ) {
            TagPill(
                text = "Use $demoEmail",
                containerColor = PizzaTheme.colors.brand.copy(alpha = 0.10f),
                contentColor = PizzaTheme.colors.brand
            )
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        label = { Text(text = label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = PizzaTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (passwordVisible) {
                        "Hide password"
                    } else {
                        "Show password"
                    },
                    tint = PizzaTheme.colors.textMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .bouncyClick(pressedScale = 0.85f, onClick = onTogglePassword)
                )
            }
        } else {
            null
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onDone?.invoke() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PizzaTheme.colors.surface,
            unfocusedContainerColor = PizzaTheme.colors.surface,
            focusedBorderColor = PizzaTheme.colors.brand,
            unfocusedBorderColor = PizzaTheme.colors.border,
            cursorColor = PizzaTheme.colors.brand,
            focusedLabelColor = PizzaTheme.colors.brand,
            unfocusedLabelColor = PizzaTheme.colors.textMuted,
            focusedTextColor = PizzaTheme.colors.textPrimary,
            unfocusedTextColor = PizzaTheme.colors.textPrimary
        )
    )
}
