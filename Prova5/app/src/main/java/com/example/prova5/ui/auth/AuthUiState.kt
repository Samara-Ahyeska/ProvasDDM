package com.example.prova5.ui.auth

sealed interface AuthUiState {
    data object Unauthenticated : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(
        val userId: String,
        val name: String,
        val email: String
    ) : AuthUiState
    data class SmsCodeSent(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class InfoMessage(val message: String) : AuthUiState
}