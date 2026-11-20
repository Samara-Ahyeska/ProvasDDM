package com.example.prova6.model

import java.util.Locale

data class ShoppingListItem(
    val id: String,
    val name: String,
    val originLabel: String,
    val quantityLabel: String,
    val estimatedPrice: Double,
    val isChecked: Boolean = false
) {
    fun formattedPrice(): String {
        val locale = Locale.forLanguageTag("pt-BR")
        return "R$ " + String.format(locale, "%.2f", estimatedPrice)
    }
}

data class ShoppingSection(
    val id: String,
    val title: String,
    val totalCount: Int,
    val items: List<ShoppingListItem>
)

data class ShoppingListSummary(
    val totalItemsCount: Int,
    val estimatedTotal: Double,
    val sections: List<ShoppingSection>
) {
    fun formattedEstimatedTotal(): String {
        val locale = Locale.forLanguageTag("pt-BR")
        return "R$ " + String.format(locale, "%.2f", estimatedTotal)
    }
}