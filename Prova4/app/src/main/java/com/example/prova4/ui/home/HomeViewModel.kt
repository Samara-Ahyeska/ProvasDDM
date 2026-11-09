package com.example.prova4.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prova4.data.model.Habit
import com.example.prova4.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class HomeUiState(
    val userName: String = "Sâmara",
    val userEmail: String = "samara@email.com",
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStartDate: LocalDate = LocalDate.now(),
    val completedCount: Int = 0,
    val maxStreak: Int = 0,
    val activeHabitsCount: Int = 0,
    val pendingHabits: List<Habit> = emptyList(),
    val completedHabits: List<Habit> = emptyList(),
    val categories: List<String> = emptyList()
)

class HomeViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val currentSunday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        _uiState.update { it.copy(selectedDate = today, weekStartDate = currentSunday) }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            launch {
                repository.habits.collect { allHabits ->
                    updateHabitsForSelectedDate(allHabits, _uiState.value.selectedDate)
                }
            }
            launch {
                repository.categories.collect { cats ->
                    _uiState.update { it.copy(categories = cats) }
                }
            }
        }
    }

    private fun updateHabitsForSelectedDate(allHabits: List<Habit>, date: LocalDate) {
        val dayOfWeek = date.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        val isWeekday = !isWeekend

        // Filtragem baseada na frequência escolhida e na data selecionada no calendário
        val filteredByFrequency = allHabits.filter { habit ->
            when (habit.frequency.lowercase().trim()) {
                "dias úteis", "segunda a sexta" -> isWeekday
                "fim de semana", "fins de semana", "sábado e domingo" -> isWeekend
                "somente hoje", "1 dia" -> habit.createdAt == date || date == LocalDate.now()
                else -> true // "Todo dia"
            }
        }.map { habit ->
            val streak = calculateStreak(habit.completedDates, date)
            habit.copy(streakDays = streak)
        }

        val pending = filteredByFrequency.filter { !it.completedDates.contains(date) && !it.isCompletedToday }
        val completed = filteredByFrequency.filter { it.completedDates.contains(date) || it.isCompletedToday }

        val totalCompleted = completed.size
        val highestStreak = allHabits.maxOfOrNull { calculateStreak(it.completedDates, date) } ?: 0

        _uiState.update {
            it.copy(
                pendingHabits = pending,
                completedHabits = completed,
                completedCount = totalCompleted,
                maxStreak = highestStreak,
                activeHabitsCount = allHabits.size
            )
        }
    }

    // Calcula dias seguidos sem pular (Streak)
    fun calculateStreak(completedDates: List<LocalDate>, refDate: LocalDate = LocalDate.now()): Int {
        var streak = 0
        var checkDate = refDate

        if (!completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (completedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    // Calcula a porcentagem de conclusão nos últimos 30 dias a partir da data de referência
    fun calculate30DaysPercentage(completedDates: List<LocalDate>, refDate: LocalDate = LocalDate.now()): Int {
        val startDate = refDate.minusDays(29)
        val countInPeriod = completedDates.count { date ->
            !date.isBefore(startDate) && !date.isAfter(refDate)
        }
        return ((countInPeriod.toFloat() / 30f) * 100).toInt().coerceIn(0, 100)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        updateHabitsForSelectedDate(repository.habits.value, date)
    }

    fun previousWeek() {
        _uiState.update { state -> state.copy(weekStartDate = state.weekStartDate.minusWeeks(1)) }
    }

    fun nextWeek() {
        _uiState.update { state -> state.copy(weekStartDate = state.weekStartDate.plusWeeks(1)) }
    }

    fun updateUserProfile(newName: String, newEmail: String) {
        _uiState.update { it.copy(userName = newName, userEmail = newEmail) }
    }

    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }
}