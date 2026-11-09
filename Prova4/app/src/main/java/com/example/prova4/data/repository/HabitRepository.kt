package com.example.prova4.data.repository

import com.example.prova4.data.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class HabitRepository {

    // Mantém os dados centralizados e compartilhados globalmente na aplicação
    companion object {
        private val _habits = MutableStateFlow<List<Habit>>(emptyList())
        private val _categories = MutableStateFlow<List<String>>(
            listOf("Todas", "Saúde", "Estudos", "Trabalho", "Mindfulness")
        )
    }

    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    fun addCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isNotBlank() && !_categories.value.contains(trimmed)) {
            _categories.update { it + trimmed }
        }
    }

    fun deleteCategory(category: String) {
        _categories.update { current -> current.filter { it != category } }
    }

    fun getHabitById(id: String): Habit? {
        return _habits.value.find { it.id == id }
    }

    fun saveHabit(habit: Habit) {
        _habits.update { currentList ->
            val index = currentList.indexOfFirst { it.id == habit.id }
            if (index >= 0) {
                currentList.toMutableList().apply { set(index, habit) }
            } else {
                currentList + habit
            }
        }
        if (habit.category.isNotBlank()) {
            addCategory(habit.category)
        }
    }

    fun deleteHabit(habitId: String) {
        _habits.update { currentList ->
            currentList.filter { it.id != habitId }
        }
    }

    /**
     * Alterna a conclusão do hábito considerando a data alvo selecionada no calendário
     */
    fun toggleHabitCompletion(habitId: String, targetDate: LocalDate = LocalDate.now()) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    val isAlreadyCompleted = habit.completedDates.contains(targetDate) ||
                            (targetDate == LocalDate.now() && habit.isCompletedToday)

                    val updatedDates = if (isAlreadyCompleted) {
                        habit.completedDates - targetDate
                    } else {
                        if (!habit.completedDates.contains(targetDate)) habit.completedDates + targetDate else habit.completedDates
                    }

                    val isToday = targetDate == LocalDate.now()
                    habit.copy(
                        isCompletedToday = if (isToday) !isAlreadyCompleted else habit.isCompletedToday,
                        completedDates = updatedDates
                    )
                } else {
                    habit
                }
            }
        }
    }

    fun updateHabitProgress(habitId: String, currentValue: Int, dailyNote: String) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    habit.copy(
                        currentValue = currentValue,
                        dailyNote = dailyNote
                    )
                } else {
                    habit
                }
            }
        }
    }

    fun updateTimerProgress(habitId: String, durationMinutes: Int, dailyNote: String) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    habit.copy(
                        currentValue = durationMinutes,
                        durationMinutes = durationMinutes,
                        dailyNote = dailyNote
                    )
                } else {
                    habit
                }
            }
        }
    }
}