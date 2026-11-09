package com.example.prova4.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumericHabitScreen(
    habitId: String,
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    val primaryColor = Color(0xFF0F766E)

    var currentValueInput by remember { mutableStateOf("") }
    var dailyNoteInput by remember { mutableStateOf("") }

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    LaunchedEffect(selectedHabit) {
        selectedHabit?.let {
            currentValueInput = it.currentValue.toString()
            dailyNoteInput = it.dailyNote
        }
    }

    val habitTitle = selectedHabit?.title ?: "Hábito Numérico"
    val targetValue = selectedHabit?.targetValue ?: 100
    val unit = selectedHabit?.unit ?: "un"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voltar", color = Color.Gray, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF7F5F0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = habitTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )

            // Card informativo de meta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Meta definida", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = "$targetValue $unit",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Campo para inserir o valor atual alcançado
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Progresso de hoje ($unit)", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = currentValueInput,
                    onValueChange = { currentValueInput = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Campo de Nota Diária
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nota do dia (opcional)", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = dailyNoteInput,
                    onValueChange = { dailyNoteInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    placeholder = { Text("Como foi sua experiência hoje?") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão Salvar Progresso
            Button(
                onClick = {
                    val numericValue = currentValueInput.toIntOrNull() ?: 0
                    viewModel.updateProgress(habitId, numericValue, dailyNoteInput)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Salvar progresso", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Botão Excluir Hábito
            OutlinedButton(
                onClick = {
                    viewModel.deleteHabit(habitId)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB3261E))
            ) {
                Text("Excluir hábito", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold)
            }
        }
    }
}