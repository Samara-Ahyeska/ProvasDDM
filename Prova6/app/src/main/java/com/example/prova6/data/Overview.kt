package com.example.prova6.data

import com.example.prova6.model.OverviewSummary
import com.example.prova6.model.RestockItem
import com.example.prova6.model.StockStatus

object Overview {
    fun initialSummary(): OverviewSummary = OverviewSummary(
        contextLabel = "Casa · Novembro",
        registeredProductsCount = 148,
        criticalOrLowCount = 9,
        outOfStockCount = 4,
        estimatedNextPurchase = 386.40,
        totalListItems = 17,
        automaticItemsCount = 13,
        manualItemsCount = 4,
        restockItems = listOf(
            RestockItem("1", "Leite integral", "Geladeira", StockStatus.OUT_OF_STOCK),
            RestockItem("2", "Papel higiênico", "Banheiro", StockStatus.OUT_OF_STOCK),
            RestockItem("3", "Arroz 5 kg", "Armário Seco", StockStatus.LOW),
            RestockItem("4", "Detergente", "Lavanderia", StockStatus.LOW)
        )
    )
}