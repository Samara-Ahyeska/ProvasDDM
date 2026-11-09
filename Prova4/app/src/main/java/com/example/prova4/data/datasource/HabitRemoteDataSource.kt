package com.example.prova4.data.datasource

import com.example.prova4.data.model.Habit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HabitRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String? get() = auth.currentUser?.uid

    private fun getUserHabitsCollection() = firestore.collection("users")
        .document(userId ?: "anonymous")
        .collection("habits")

    // Buscar todos os hábitos do usuário no Firestore
    suspend fun fetchHabits(): List<Habit> {
        if (userId == null) return emptyList()
        val snapshot = getUserHabitsCollection().get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Habit::class.java)?.copy(id = doc.id)
        }
    }

    // Salvar ou atualizar um hábito no Firestore
    suspend fun saveHabit(habit: Habit) {
        if (userId == null) return
        getUserHabitsCollection().document(habit.id).set(habit).await()
    }

    // Deletar um hábito do Firestore
    suspend fun deleteHabit(habitId: String) {
        if (userId == null) return
        getUserHabitsCollection().document(habitId).delete().await()
    }
}