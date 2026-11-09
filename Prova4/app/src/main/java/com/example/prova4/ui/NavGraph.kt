package com.example.prova4.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prova4.ui.detail.CreateEditHabitScreen
import com.example.prova4.ui.detail.DetailViewModel
import com.example.prova4.ui.home.HomeScreen
import com.example.prova4.ui.home.HomeViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Instância compartilhada do HomeViewModel para manter o estado da lista e do calendário atualizado
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home_route") {

        // Tela Inicial (Home)
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
                    // Adicione aqui a rota de perfil se necessário, ex: navController.navigate("profile_route")
                }
            )
        }

        // Tela de Criação ou Edição de Hábito (Com ID opcional)
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

            CreateEditHabitScreen(
                habitId = habitId,
                viewModel = detailViewModel,
                homeViewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Rota auxiliar para criação sem parâmetro na URL
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