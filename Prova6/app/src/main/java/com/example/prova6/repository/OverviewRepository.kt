package com.example.prova6.repository

import com.example.prova6.data.Overview
import com.example.prova6.model.OverviewSummary

class OverviewRepository {
    fun getOverviewSummary(): OverviewSummary {
        return Overview.initialSummary()
    }
}