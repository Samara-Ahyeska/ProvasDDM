package com.example.prova6.repository

import com.example.prova6.data.AislesMock
import com.example.prova6.model.AisleProductTemplate
import com.example.prova6.model.MarketAisleCard

class AislesRepository {
    fun getAisles(): List<MarketAisleCard> = AislesMock.initialAisles()

    fun getProducts(): List<AisleProductTemplate> = AislesMock.initialProducts()
}
