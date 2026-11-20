package com.example.prova6.model

enum class AisleGroup(val title: String) {
    FOOD_AND_DRINKS("Alimentos e bebidas"),
    NON_FOOD("Não alimentos")
}

data class AisleProductTemplate(
    val id: String,
    val aisleId: String,
    val name: String,
    val defaultQuantityLabel: String,
    val estimatedPrice: Double
)

data class MarketAisleCard(
    val id: String,
    val name: String,
    val description: String,
    val group: AisleGroup,
    val sectionHeaderPrefix: String = name.uppercase()
) {
    fun sectionHeaderTitle(itemCount: Int): String = "$sectionHeaderPrefix · $itemCount"
}