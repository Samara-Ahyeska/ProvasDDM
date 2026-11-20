package com.example.prova6.data

import com.example.prova6.model.ShoppingListItem
import com.example.prova6.model.ShoppingListSummary
import com.example.prova6.model.ShoppingSection

object ShoppingListMock {

    fun initialSummary(): ShoppingListSummary = ShoppingListSummary(
        totalItemsCount = 17,
        estimatedTotal = 386.40,
        sections = listOf(
            ShoppingSection(
                id = "hortifruti",
                title = "HORTIFRÚTI / FEIRA · 4",
                totalCount = 4,
                items = listOf(
                    ShoppingListItem("1", "Tomate", "Auto · Esgotado", "2 un", 7.49),
                    ShoppingListItem("2", "Banana", "Avulso", "1 dz", 5.90)
                )
            ),
            ShoppingSection(
                id = "padaria",
                title = "PADARIA & MATINAIS · 2",
                totalCount = 2,
                items = listOf(
                    ShoppingListItem("3", "Pão de forma", "Auto · Baixo", "1 un", 8.50)
                )
            ),
            ShoppingSection(
                id = "frios",
                title = "FRIOS & LATICÍNIOS · 3",
                totalCount = 3,
                items = listOf(
                    ShoppingListItem("4", "Leite integral", "Auto · Esgotado", "6 un", 5.29)
                )
            ),
            ShoppingSection(
                id = "mercearia",
                title = "MERCEARIA SECA · 5",
                totalCount = 5,
                items = listOf(
                    ShoppingListItem("5", "Arroz 5 kg", "Auto · Baixo", "1 un", 29.90),
                    ShoppingListItem("6", "Macarrão", "Auto · Baixo", "3 un", 4.79)
                )
            ),
            ShoppingSection(
                id = "bebidas",
                title = "BEBIDAS - 1",
                totalCount = 1,
                items = listOf(
                    ShoppingListItem("7", "Suco de uva", "Avulso", "2 un", 12.90)
                )
            ),
            ShoppingSection(
                id = "higiene",
                title = "HIGIENE & LIMPEZA · 2",
                totalCount = 2,
                items = listOf(
                    ShoppingListItem("8", "Papel higiênico", "Auto · Esgotado", "1 pct", 24.90)
                )
            )
        )
    )
}