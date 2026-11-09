package com.example.prova4.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prova4.data.model.HabitType
import com.example.prova4.ui.auth.AuthScreen
import com.example.prova4.ui.auth.AuthViewModel
import com.example.prova4.ui.detail.CreateEditHabitScreen
import com.example.prova4.ui.detail.DetailViewModel
import com.example.prova4.ui.detail.EditNumericHabitScreen
import com.example.prova4.ui.detail.EditTimerHabitScreen
import com.example.prova4.ui.home.HomeScreen
import com.example.prova4.ui.home.HomeViewModel
import com.example.prova4.ui.profile.ProfileScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login_route") {

        composable("login_route") {
            AuthScreen(
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                onAuthSuccess = {
                    homeViewModel.loadUserDataFromFirebase()
                    navController.navigate("home_route") {
                        popUpTo("login_route") { inclusive = true }
                    }
                }
            )
        }

        composable("home_route") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreateHabit = {
                    navController.navigate("create_edit_habit_route/")
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate("create_edit_habit_route/$habitId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile_route")
                }
            )
        }

        composable("profile_route") {
            ProfileScreen(
                homeViewModel = homeViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login_route") {
                        popUpTo("login_route") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "create_edit_habit_route/{habitId}",
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            val detailViewModel: DetailViewModel = viewModel()

            if (!habitId.isNullOrBlank()) {
                LaunchedEffect(habitId) {
                    detailViewModel.loadHabit(habitId)
                }

                val selectedHabit by detailViewModel.selectedHabit.collectAsState()

                when (selectedHabit?.type) {
                    HabitType.NUMERICO -> {
                        EditNumericHabitScreen(
                            habitId = habitId,
                            viewModel = detailViewModel,
                            homeViewModel = homeViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    HabitType.TIMER -> {
                        EditTimerHabitScreen(
                            habitId = habitId,
                            viewModel = detailViewModel,
                            homeViewModel = homeViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    else -> {
                        CreateEditHabitScreen(
                            habitId = habitId,
                            viewModel = detailViewModel,
                            homeViewModel = homeViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            } else {
                CreateEditHabitScreen(
                    habitId = null,
                    viewModel = detailViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("create_edit_habit_route/") {
            val detailViewModel: DetailViewModel = viewModel()

            CreateEditHabitScreen(
                habitId = null,
                viewModel = detailViewModel,
                homeViewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}