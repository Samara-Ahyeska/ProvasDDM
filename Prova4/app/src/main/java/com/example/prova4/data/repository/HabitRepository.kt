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
        if (category.isNotBlank() && !_categories.value.contains(category)) {
            _categories.update { it + category }
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

    fun toggleHabitCompletion(habitId: String) {
        val today = LocalDate.now()
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    val isCompleted = !habit.isCompletedToday
                    val updatedDates = if (isCompleted) {
                        if (!habit.completedDates.contains(today)) habit.completedDates + today else habit.completedDates
                    } else {
                        habit.completedDates - today
                    }
                    habit.copy(
                        isCompletedToday = isCompleted,
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