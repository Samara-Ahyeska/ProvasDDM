package com.example.prova5.ui.dashboard

import com.example.prova5.data.model.ExpenseCategory
import com.example.prova5.data.model.ExpenseItem

enum class AlertLevel { GREEN, YELLOW, RED }

data class CategoryCardUi(
    val category: ExpenseCategory,
    val spent: Double,
    val limit: Double,
    val progress: Float,
    val percentage: Int,
    val alertLevel: AlertLevel
)

data class DashboardUiState(
    val monthYearLabel: String = "Novembro 2026",
    val totalSpent: Double = 0.0,
    val totalBudget: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val budgetedCategories: List<CategoryCardUi> = emptyList(),
    val othersSpent: Double = 0.0,
    val expenses: List<ExpenseItem> = emptyList()
)