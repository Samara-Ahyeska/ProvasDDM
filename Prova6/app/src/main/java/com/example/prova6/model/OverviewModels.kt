package com.example.prova6.model

import java.util.Locale

enum class StockStatus(val label: String) {
    OUT_OF_STOCK("Esgotado"),
    LOW("Baixo")
}

data class RestockItem(
    val id: String,
    val name: String,
    val location: String,
    val status: StockStatus
)

data class OverviewSummary(
    val contextLabel: String,
    val registeredProductsCount: Int,
    val criticalOrLowCount: Int,
    val outOfStockCount: Int,
    val estimatedNextPurchase: Double,
    val totalListItems: Int,
    val automaticItemsCount: Int,
    val manualItemsCount: Int,
    val restockItems: List<RestockItem>
) {
    fun formattedCurrency(): String {
        val brLocale = Locale.forLanguageTag("pt-BR")
        return "R$ " + String.format(brLocale, "%.2f", estimatedNextPurchase)
    }
}