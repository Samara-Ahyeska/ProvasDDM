package com.example.prova6.repository

import com.example.prova6.data.ShoppingListMock
import com.example.prova6.model.ShoppingListSummary

class ShoppingListRepository {
    fun getShoppingListSummary(): ShoppingListSummary = ShoppingListMock.initialSummary()
}