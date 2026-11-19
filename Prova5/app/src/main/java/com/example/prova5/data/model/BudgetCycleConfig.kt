package com.example.prova5.data.model

import java.util.Calendar

enum class CycleResetMode(val label: String, val description: String) {
    EVERY_30_DAYS(
        label = "A cada 30 dias",
        description = "Renova a cada 30 dias a partir da data base"
    ),
    EVERY_15_DAYS(
        label = "A cada 15 dias",
        description = "Renova a cada 15 dias a partir da data base (ideal para vale)"
    )
}

data class BudgetCycleConfig(
    val mode: CycleResetMode = CycleResetMode.EVERY_30_DAYS,
    val anchorDateMillis: Long = System.currentTimeMillis(),
    val cycleResetAtMillis: Long? = null
)

fun computeCurrentCycleStart(
    config: BudgetCycleConfig,
    nowMillis: Long = System.currentTimeMillis()
): Long {
    val cycleDays = when (config.mode) {
        CycleResetMode.EVERY_30_DAYS -> 30L
        CycleResetMode.EVERY_15_DAYS -> 15L
    }
    val cycleDurationMillis = cycleDays * 24L * 60L * 60L * 1000L

    val anchorCal = Calendar.getInstance().apply {
        timeInMillis = config.anchorDateMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfAnchor = anchorCal.timeInMillis

    val computedStart = if (nowMillis <= startOfAnchor) {
        startOfAnchor
    } else {
        val elapsed = nowMillis - startOfAnchor
        val cyclesPassed = elapsed / cycleDurationMillis
        startOfAnchor + (cyclesPassed * cycleDurationMillis)
    }

    val manualReset = config.cycleResetAtMillis
    return if (manualReset != null && manualReset > computedStart) {
        manualReset
    } else {
        computedStart
    }
}