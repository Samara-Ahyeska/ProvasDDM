package com.example.prova6.data

import com.example.prova6.model.PantryItem
import com.example.prova6.model.PantryLevel

object PantryMock {
    val locations = listOf("Todos", "Geladeira", "Armário Seco", "Balcão", "Banheiro", "Lavanderia")

    fun initialItems(): List<PantryItem> = listOf(
        PantryItem(
            id = "1",
            name = "Leite integral",
            location = "Geladeira",
            defaultQuantityLabel = "padrão 6 un",
            level = PantryLevel.OUT_OF_STOCK,
            isExpanded = true
        ),
        PantryItem(
            id = "2",
            name = "Ovos",
            location = "Geladeira",
            defaultQuantityLabel = "padrão 12 un",
            level = PantryLevel.MEDIUM
        ),
        PantryItem(
            id = "3",
            name = "Arroz 5 kg",
            location = "Armário Seco",
            defaultQuantityLabel = "padrão 1 un",
            level = PantryLevel.LOW
        ),
        PantryItem(
            id = "4",
            name = "Café",
            location = "Armário Seco",
            defaultQuantityLabel = "padrão 2 un",
            level = PantryLevel.FULL
        ),
        PantryItem(
            id = "5",
            name = "Papel higiênico",
            location = "Banheiro",
            defaultQuantityLabel = "padrão 12 rolos",
            level = PantryLevel.OUT_OF_STOCK
        ),
        PantryItem(
            id = "6",
            name = "Detergente",
            location = "Lavanderia",
            defaultQuantityLabel = "padrão 3 un",
            level = PantryLevel.LOW
        )
    )
}