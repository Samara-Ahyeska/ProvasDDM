package com.example.prova4.data.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val totalCompletedHabits: Int = 0,
    val maxStreak: Int = 0,
    val activeHabitsCount: Int = 0
)