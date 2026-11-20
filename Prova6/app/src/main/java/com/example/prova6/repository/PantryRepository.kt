package com.example.prova6.repository

import com.example.prova6.data.PantryMock
import com.example.prova6.model.PantryItem

class PantryRepository {
    fun getLocations(): List<String> = PantryMock.locations

    fun getPantryItems(): List<PantryItem> = PantryMock.initialItems()
}