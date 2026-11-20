package com.example.prova6.model

enum class PantryLevel(
    val label: String,
    val percentageLabel: String
) {
    FULL("Cheio", "100%"),
    MEDIUM("Médio", "50%"),
    LOW("Baixo", "25%"),
    OUT_OF_STOCK("Esgotado", "0%")
}

data class PantryItem(
    val id: String,
    val name: String,
    val location: String,
    val defaultQuantityLabel: String,
    val level: PantryLevel,
    val isExpanded: Boolean = false
)