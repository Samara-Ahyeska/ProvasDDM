package com.example.prova4.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "habit_local_prefs")

class HabitLocalDataSource(private val context: Context) {


    fun getHabitCompletionStatus(habitId: String, dateStr: String): Flow<Boolean> {
        val preferenceKey = booleanPreferencesKey("habit_${habitId}_$dateStr")
        return context.dataStore.data.map { preferences ->
            preferences[preferenceKey] ?: false
        }
    }


    suspend fun saveHabitCompletionStatus(habitId: String, dateStr: String, isCompleted: Boolean) {
        val preferenceKey = booleanPreferencesKey("habit_${habitId}_$dateStr")
        context.dataStore.edit { preferences ->
            preferences[preferenceKey] = isCompleted
        }
    }
}