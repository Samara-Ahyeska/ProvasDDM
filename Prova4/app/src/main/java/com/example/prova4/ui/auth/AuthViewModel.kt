package com.example.prova4.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prova4.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun isUserLoggedIn(): Boolean = authRepository.currentUser != null

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, pass)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.localizedMessage ?: "Erro ao entrar.") }
        }
    }

    fun register(email: String, pass: String, confirmPass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos.")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("As senhas não coincidem.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, pass, name)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.localizedMessage ?: "Erro ao registrar.") }
        }
    }

    // Adicionado para suportar o encerramento de sessão na ProfileScreen / NavGraph
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Idle
    }
}