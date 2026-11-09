package com.example.prova4.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prova4.data.model.Habit
import com.example.prova4.data.model.matchesFrequency
import com.example.prova4.data.repository.AuthRepository
import com.example.prova4.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class HomeUiState(
    val userName: String = "Usuário",
    val userEmail: String = "",
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
    private val repository: HabitRepository = HabitRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val currentSunday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        _uiState.update { it.copy(selectedDate = today, weekStartDate = currentSunday) }

        // Carrega os dados reais do usuário do Firebase ao inicializar
        loadUserDataFromFirebase()
        observeData()
    }

    fun loadUserDataFromFirebase() {
        val userData = authRepository.getCurrentUserData()
        if (userData != null) {
            _uiState.update { state ->
                state.copy(
                    userName = userData.name.ifBlank { "Usuário" },
                    userEmail = userData.email
                )
            }
        }
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
        val filteredByFrequency = allHabits.filter { habit ->
            matchesFrequency(
                frequency = habit.frequency,
                targetDate = date,
                creationDate = habit.createdAt
            )
        }.map { habit ->
            val streak = calculateStreak(habit.completedDates, date)
            val isCompletedOnDate = habit.completedDates.contains(date)
            habit.copy(
                streakDays = streak,
                isCompletedToday = isCompletedOnDate
            )
        }

        val pending = filteredByFrequency.filter { !it.isCompletedToday }
        val completed = filteredByFrequency.filter { it.isCompletedToday }

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

    // Atualiza nome de exibição localmente e no Firebase de forma assíncrona
    fun updateUserName(newName: String) {
        _uiState.update { it.copy(userName = newName) }
        viewModelScope.launch {
            authRepository.updateProfile(newName)
        }
    }

    // Atualiza o estado da UI com nome e e-mail
    fun updateUserProfile(newName: String, newEmail: String) {
        _uiState.update { it.copy(userName = newName, userEmail = newEmail) }
    }

    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _uiState.value.selectedDate)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun addCategory(category: String) {
        repository.addCategory(category)
    }

    fun deleteCategory(category: String) {
        repository.deleteCategory(category)
    }
}