package com.example.prova4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.prova4.ui.navigation.NavGraph
import com.example.prova4.ui.theme.HabitTrackerTheme // <-- Importando o nome correto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme { // <-- Usando a função correta definida no Theme.kt
                NavGraph()
            }
        }
    }
}