package com.example.prova4.data.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class HabitPeriod {
    MANHA, TARDE, NOITE
}

enum class HabitType {
    SIMPLES, NUMERICO, TIMER
}

data class Habit(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "Geral",
    val frequency: String = "Todo dia", // Options: "Todo dia", "Dias úteis", "Fim de semana", "Somente hoje"
    val createdAt: LocalDate = LocalDate.now(),
    val periods: List<HabitPeriod> = listOf(HabitPeriod.MANHA),
    val type: HabitType = HabitType.SIMPLES,
    val isCompletedToday: Boolean = false,
    val completedDates: List<LocalDate> = emptyList(),
    val streakDays: Int = 0,
    val reminderTime: String = "",
    val hasReminder: Boolean = false,
    val dailyNote: String = "",
    val targetValue: Int = 0,
    val currentValue: Int = 0,
    val durationMinutes: Int = 0,
    val unit: String = ""
)


fun matchesFrequency(frequency: String, targetDate: LocalDate, creationDate: LocalDate): Boolean {
    if (targetDate.isBefore(creationDate)) return false

    val dayOfWeek = targetDate.dayOfWeek
    return when (frequency.trim().lowercase()) {
        "todo dia", "todos os dias" -> true
        "dias úteis", "dias uteis" -> dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY
        "fim de semana", "fins de semana" -> dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        "somente hoje" -> targetDate == creationDate
        else -> true
    }
}