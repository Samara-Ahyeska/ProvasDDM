package com.example.prova4.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prova4.data.model.Habit
import com.example.prova4.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _selectedHabit = MutableStateFlow<Habit?>(null)
    val selectedHabit: StateFlow<Habit?> = _selectedHabit.asStateFlow()

    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId)
            _selectedHabit.value = habit
        }
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            repository.saveHabit(habit)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId)
            loadHabit(habitId) // Atualiza o estado local após alternar
        }
    }

    fun updateProgress(habitId: String, currentValue: Int, dailyNote: String) {
        viewModelScope.launch {
            repository.updateHabitProgress(habitId, currentValue, dailyNote)
            loadHabit(habitId)
        }
    }

    fun updateTimerProgress(habitId: String, durationMinutes: Int, dailyNote: String) {
        viewModelScope.launch {
            repository.updateTimerProgress(habitId, durationMinutes, dailyNote)
            loadHabit(habitId)
        }
    }
}