package com.example.prova5.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prova5.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(
        authRepository.currentUserId?.let { uid ->
            AuthUiState.Authenticated(
                userId = uid,
                name = authRepository.currentUserName.orEmpty(),
                email = authRepository.currentUserEmail.orEmpty()
            )
        } ?: AuthUiState.Unauthenticated
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateLoginInputs(email, password)) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { uid ->
                    _uiState.value = AuthUiState.Authenticated(
                        userId = uid,
                        name = authRepository.currentUserName.orEmpty(),
                        email = email.trim()
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error("E-mail ou senha incorretos.")
                }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (!validateRegisterInputs(name, email, password, confirmPassword)) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.register(name, email, password)
                .onSuccess { uid ->
                    _uiState.value = AuthUiState.Authenticated(
                        userId = uid,
                        name = name.trim(),
                        email = email.trim()
                    )
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.localizedMessage ?: "Não foi possível criar a conta."
                    )
                }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Digite seu e-mail acima para recuperar a senha.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = AuthUiState.InfoMessage(
                        "Enviamos um link de redefinição para ${email.trim()}."
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error("Erro ao enviar e-mail de recuperação.")
                }
        }
    }

    fun clearFeedback() {
        if (_uiState.value is AuthUiState.Error || _uiState.value is AuthUiState.InfoMessage) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Unauthenticated
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Informe um endereço de e-mail válido.")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("A senha deve ter no mínimo 6 caracteres.")
            return false
        }
        return true
    }

    private fun validateRegisterInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Informe seu nome.")
            return false
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Informe um endereço de e-mail válido.")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("A senha deve ter pelo menos 6 caracteres.")
            return false
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("As senhas não coincidem.")
            return false
        }
        return true
    }
}