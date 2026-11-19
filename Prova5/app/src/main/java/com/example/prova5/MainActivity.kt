package com.example.prova5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prova5.ui.auth.AuthScreen
import com.example.prova5.ui.auth.AuthUiState
import com.example.prova5.ui.auth.AuthViewModel
import com.example.prova5.ui.dashboard.DashboardScreen
import com.example.prova5.ui.dashboard.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val dashboardViewModel: DashboardViewModel = viewModel()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()

            when (val state = authState) {
                is AuthUiState.Authenticated -> {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        userName = state.name,
                        userEmail = state.email,
                        onLogout = { authViewModel.logout() },
                        onUpdateDisplayName = { newName, onResult ->
                            authViewModel.updateDisplayName(newName, onResult)
                        },
                        onChangePasswordAndLogout = { currentPassword, newPassword, onResult ->
                            authViewModel.updatePasswordAndLogout(currentPassword, newPassword, onResult)
                        }
                    )
                }
                else -> {
                    AuthScreen(viewModel = authViewModel)
                }
            }
        }
    }
}