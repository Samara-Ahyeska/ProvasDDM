package com.example.prova6.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prova6.data.ShoppingListMock
import com.example.prova6.model.AisleProductTemplate
import com.example.prova6.model.MarketAisleCard
import com.example.prova6.model.ShoppingListItem
import com.example.prova6.model.ShoppingSection
import com.example.prova6.ui.screens.AislesRoute
import com.example.prova6.ui.screens.OverviewRoute
import com.example.prova6.ui.screens.PantryRoute
import com.example.prova6.ui.screens.ShoppingListScreen

object ListfyRoutes {
    const val OVERVIEW = "overview"
    const val PANTRY = "pantry"
    const val SHOPPING_LIST = "shopping_list"
    const val AISLES = "aisles"
    const val METRICS = "metrics"
    const val SUPERMARKET_MODE = "supermarket_mode"
}

@Composable
fun ListfyNavGraph(
    navController: NavHostController = rememberNavController()
) {
    var shoppingSummary by remember { mutableStateOf(ShoppingListMock.initialSummary()) }
    var quickItemName by remember { mutableStateOf("") }

    val navigateToBottomTab: (Int) -> Unit = { index ->
        val targetRoute = when (index) {
            0 -> ListfyRoutes.OVERVIEW
            1 -> ListfyRoutes.PANTRY
            2 -> ListfyRoutes.SHOPPING_LIST
            3 -> ListfyRoutes.METRICS
            else -> ListfyRoutes.OVERVIEW
        }

        if (
            targetRoute == ListfyRoutes.OVERVIEW ||
            targetRoute == ListfyRoutes.PANTRY ||
            targetRoute == ListfyRoutes.SHOPPING_LIST
        ) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val addProductFromAisle: (MarketAisleCard, AisleProductTemplate) -> Unit = { aisle, product ->
        val newItem = ShoppingListItem(
            id = "aisle_item_${System.currentTimeMillis()}",
            name = product.name,
            originLabel = "Corredor · ${aisle.name}",
            quantityLabel = product.defaultQuantityLabel,
            estimatedPrice = product.estimatedPrice
        )

        val existingIndex = shoppingSummary.sections.indexOfFirst { it.id == aisle.id }
        val updatedSections = if (existingIndex >= 0) {
            shoppingSummary.sections.mapIndexed { idx, section ->
                if (idx == existingIndex) {
                    val nextCount = section.totalCount + 1
                    section.copy(
                        title = aisle.sectionHeaderTitle(nextCount),
                        totalCount = nextCount,
                        items = section.items + newItem
                    )
                } else {
                    section
                }
            }
        } else {
            shoppingSummary.sections + ShoppingSection(
                id = aisle.id,
                title = aisle.sectionHeaderTitle(1),
                totalCount = 1,
                items = listOf(newItem)
            )
        }

        shoppingSummary = shoppingSummary.copy(
            totalItemsCount = shoppingSummary.totalItemsCount + 1,
            estimatedTotal = shoppingSummary.estimatedTotal + product.estimatedPrice,
            sections = updatedSections
        )
    }

    NavHost(
        navController = navController,
        startDestination = ListfyRoutes.OVERVIEW
    ) {
        composable(ListfyRoutes.OVERVIEW) {
            OverviewRoute(
                onStartSupermarketMode = {
                    // navController.navigate(ListfyRoutes.SUPERMARKET_MODE)
                },
                onNavigateToTab = navigateToBottomTab
            )
        }

        composable(ListfyRoutes.PANTRY) {
            PantryRoute(
                onAddNewClick = {
                    // Abertura do formulário/modal de novo item da despensa
                },
                onNavigateToTab = navigateToBottomTab
            )
        }

        composable(ListfyRoutes.SHOPPING_LIST) {
            ShoppingListScreen(
                summary = shoppingSummary,
                quickNameInput = quickItemName,
                selectedTab = 2,
                onQuickNameChange = { quickItemName = it },
                onOpenManualModal = {
                    navController.navigate(ListfyRoutes.AISLES)
                },
                onToggleItemChecked = { itemId ->
                    val updatedSections = shoppingSummary.sections.map { section ->
                        section.copy(
                            items = section.items.map { item ->
                                if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                            }
                        )
                    }
                    shoppingSummary = shoppingSummary.copy(sections = updatedSections)
                },
                onSelectTab = navigateToBottomTab
            )
        }

        composable(ListfyRoutes.AISLES) {
            AislesRoute(
                onBack = { navController.popBackStack() },
                onAddProductToShoppingList = { aisle, product ->
                    addProductFromAisle(aisle, product)
                }
            )
        }
    }
}