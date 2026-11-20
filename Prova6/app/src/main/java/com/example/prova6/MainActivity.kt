package com.example.prova6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.prova6.ui.navigation.ListfyNavGraph
import com.example.prova6.ui.theme.ListfyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListfyTheme {
                ListfyNavGraph()
            }
        }
    }
}