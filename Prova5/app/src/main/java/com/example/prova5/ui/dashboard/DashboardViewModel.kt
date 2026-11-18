package com.example.prova5.ui.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.prova5.data.model.ExpenseCategory
import com.example.prova5.data.model.ExpenseItem
import com.example.prova5.data.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

class DashboardViewModel : ViewModel() {

    private val budgetsMap = mutableMapOf(
        ExpenseCategory.SUPERMERCADO to 1200.0,
        ExpenseCategory.COMBUSTIVEL to 500.0,
        ExpenseCategory.FARMACIA to 250.0
    )

    private val expensesList = mutableListOf(
        ExpenseItem(
            id = "1",
            amount = 720.0,
            category = ExpenseCategory.SUPERMERCADO,
            description = "Supermercado Atacadão",
            paymentMethod = PaymentMethod.DEBITO
        ),
        ExpenseItem(
            id = "2",
            amount = 410.0,
            category = ExpenseCategory.COMBUSTIVEL,
            description = "Posto Shell",
            paymentMethod = PaymentMethod.PIX
        ),
        ExpenseItem(
            id = "3",
            amount = 287.40,
            category = ExpenseCategory.FARMACIA,
            description = "Drogasil",
            paymentMethod = PaymentMethod.CREDITO,
            paymentDetails = "Crédito à vista"
        )
    )

    val categoriesList = mutableStateOf(ExpenseCategory.defaultList)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        recalculateState()
    }

    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        description: String,
        paymentMethod: PaymentMethod,
        paymentDetails: String,
        timestamp: Long
    ) {
        if (amount <= 0.0) return

        expensesList.add(
            0,
            ExpenseItem(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category,
                description = description.trim().ifBlank { category.title },
                paymentMethod = paymentMethod,
                paymentDetails = paymentDetails,
                timestamp = timestamp
            )
        )
        recalculateState()
    }

    fun deleteExpense(id: String) {
        expensesList.removeAll { it.id == id }
        recalculateState()
    }

    fun updateCategoryBudget(category: ExpenseCategory, newLimit: Double) {
        if (newLimit > 0.0) {
            budgetsMap[category] = newLimit

            val currentList = categoriesList.value.toMutableList()
            val index = currentList.indexOfFirst { it.name == category.name }
            if (index != -1) {
                currentList[index] = category
            } else {
                currentList.add(category)
            }
            categoriesList.value = currentList

            for (i in expensesList.indices) {
                if (expensesList[i].category.name == category.name) {
                    expensesList[i] = expensesList[i].copy(category = category)
                }
            }

            recalculateState()
        }
    }

    private fun recalculateState() {
        val rawMonth = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(Date())
        val formattedMonth = rawMonth.replaceFirstChar { it.uppercase() }

        val budgetedCategories = categoriesList.value
            .filter { it.hasBudget }
            .map { cat ->
                val spent = expensesList.filter { it.category.name == cat.name }.sumOf { it.amount }
                val limit = budgetsMap[cat] ?: 0.0
                val ratio = if (limit > 0.0) spent / limit else 0.0
                val percentage = (ratio * 100).roundToInt()

                val alertLevel = when {
                    percentage > 90 -> AlertLevel.RED
                    percentage >= 61 -> AlertLevel.YELLOW
                    else -> AlertLevel.GREEN
                }

                CategoryCardUi(
                    category = cat,
                    spent = spent,
                    limit = limit,
                    progress = ratio.toFloat().coerceIn(0f, 1f),
                    percentage = percentage,
                    alertLevel = alertLevel
                )
            }

        val totalSpent = budgetedCategories.sumOf { it.spent }
        val totalBudget = budgetsMap.values.sum()
        val remainingBalance = (totalBudget - totalSpent).coerceAtLeast(0.0)

        _uiState.value = DashboardUiState(
            monthYearLabel = formattedMonth,
            totalSpent = totalSpent,
            totalBudget = totalBudget,
            remainingBalance = remainingBalance,
            budgetedCategories = budgetedCategories,
            othersSpent = 0.0,
            expenses = expensesList.toList()
        )
    }
    fun deleteCategoryBudget(category: ExpenseCategory) {
        budgetsMap.remove(category)
        categoriesList.value = categoriesList.value.filter { it.name != category.name }
        expensesList.removeAll { it.category.name == category.name }
        recalculateState()
    }
}