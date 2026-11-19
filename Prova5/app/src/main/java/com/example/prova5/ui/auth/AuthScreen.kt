package com.example.prova5.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prova5.R

private val EmeraldPrimary = Color(0xFF136F48)
private val ScreenBackground = Color(0xFFF8F9FA)
private val TextPrimary = Color(0xFF1A1C1E)
private val TextSecondary = Color(0xFF6C757D)
private val InputBorder = Color(0xFFE0E4E8)

@Composable
fun AuthScreen(
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBackground
    ) {
        if (isRegisterMode) {
            RegisterContent(
                uiState = uiState,
                onRegister = { name, email, pass, confirmPass ->
                    viewModel.register(name, email, pass, confirmPass)
                },
                onNavigateToLogin = {
                    isRegisterMode = false
                    viewModel.clearFeedback()
                },
                onClearFeedback = { viewModel.clearFeedback() }
            )
        } else {
            LoginContent(
                uiState = uiState,
                onLogin = { email, pass -> viewModel.login(email, pass) },
                onResetPassword = { email -> viewModel.resetPassword(email) },
                onNavigateToRegister = {
                    isRegisterMode = true
                    viewModel.clearFeedback()
                },
                onClearFeedback = { viewModel.clearFeedback() }
            )
        }
    }
}

@Composable
private fun AppLogo(size: androidx.compose.ui.unit.Dp = 72.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo Essential Day",
            modifier = Modifier
                .fillMaxSize()
                .scale(1.5f)
        )
    }
}

@Composable
private fun LoginContent(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onResetPassword: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearFeedback: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        AppLogo()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Essential Day",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Seu dia a dia no limite certo",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        LabeledInputField(
            label = "E-mail",
            value = email,
            onValueChange = {
                email = it
                onClearFeedback()
            },
            placeholder = "voce@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(18.dp))

        LabeledInputField(
            label = "Senha",
            value = password,
            onValueChange = {
                password = it
                onClearFeedback()
            },
            placeholder = "••••••••",
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "Ocultar" else "Mostrar",
                    color = EmeraldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .clickable { passwordVisible = !passwordVisible }
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Esqueci minha senha",
                color = EmeraldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    onResetPassword(email)
                }
            )
        }

        FeedbackBanner(uiState = uiState)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Entrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Não tem conta? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Criar conta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    onNavigateToRegister()
                }
            )
        }
    }
}

@Composable
private fun RegisterContent(
    uiState: AuthUiState,
    onRegister: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearFeedback: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = !isLoading) { onNavigateToLogin() }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = EmeraldPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Voltar",
                color = EmeraldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Criar conta",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Cadastre-se com e-mail e senha para começar a controlar seus gastos.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        LabeledInputField(
            label = "Nome",
            value = name,
            onValueChange = {
                name = it
                onClearFeedback()
            },
            placeholder = "Seu nome",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledInputField(
            label = "E-mail",
            value = email,
            onValueChange = {
                email = it
                onClearFeedback()
            },
            placeholder = "voce@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledInputField(
            label = "Senha",
            value = password,
            onValueChange = {
                password = it
                onClearFeedback()
            },
            placeholder = "Mínimo 6 caracteres",
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "Ocultar" else "Mostrar",
                    color = EmeraldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .clickable { passwordVisible = !passwordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledInputField(
            label = "Confirmar senha",
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                onClearFeedback()
            },
            placeholder = "Repita a senha",
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A senha deve ter pelo menos 6 caracteres.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        FeedbackBanner(uiState = uiState)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRegister(name, email, password, confirmPassword) },
            enabled = !isLoading,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Cadastrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Já tem conta? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Entrar",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    onNavigateToLogin()
                }
            )
        }
    }
}

@Composable
private fun LabeledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = InputBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FeedbackBanner(uiState: AuthUiState) {
    when (uiState) {
        is AuthUiState.Error -> {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
        is AuthUiState.InfoMessage -> {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.message,
                color = EmeraldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        else -> Unit
    }
}