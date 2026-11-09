package com.example.prova4.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensão para inicializar o DataStore na aplicação
private val Context.dataStore by preferencesDataStore(name = "habit_local_prefs")

class HabitLocalDataSource(private val context: Context) {

    // Recupera o status de conclusão de um hábito em uma data específica
    fun getHabitCompletionStatus(habitId: String, dateStr: String): Flow<Boolean> {
        val preferenceKey = booleanPreferencesKey("habit_${habitId}_$dateStr")
        return context.dataStore.data.map { preferences ->
            preferences[preferenceKey] ?: false
        }
    }

    // Salva ou altera o status de conclusão localmente
    suspend fun saveHabitCompletionStatus(habitId: String, dateStr: String, isCompleted: Boolean) {
        val preferenceKey = booleanPreferencesKey("habit_${habitId}_$dateStr")
        context.dataStore.edit { preferences ->
            preferences[preferenceKey] = isCompleted
        }
    }
}