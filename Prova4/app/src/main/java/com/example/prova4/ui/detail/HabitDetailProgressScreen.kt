package com.example.prova4.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova4.data.model.HabitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailProgressScreen(
    habitId: String,
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val habit by viewModel.selectedHabit.collectAsState()

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = Color(0xFFFBFBFA)

    var currentValue by remember { mutableStateOf(0) }
    var dailyNote by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(habit) {
        habit?.let {
            currentValue = it.currentValue
            dailyNote = it.dailyNote
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(habit?.title ?: "Detalhes do Hábito", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1D1B20))
                        habit?.let {
                            Text(text = "Meta: ${it.targetValue} ${it.unit} · ${it.category}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF1D1B20))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(habitId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = primaryColor)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        habit?.let { currentHabit ->
            val target = if (currentHabit.targetValue > 0) currentHabit.targetValue else 2000
            val progressFraction = if (target > 0) (currentValue.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Gráfico Circular de Progresso exato conforme a imagem
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE5E7EB),
                        strokeWidth = 14.dp
                    )
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = primaryColor,
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentValue.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "de $target ${currentHabit.unit}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botões de Menos e Mais (Incremento de 250ml)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { currentValue = maxOf(0, currentValue - 250) },
                        modifier = Modifier.size(64.dp).height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }

                    Button(
                        onClick = { currentValue += 250 },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("+ 250 ${currentHabit.unit}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Seção Nota do dia
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Nota do dia", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    OutlinedTextField(
                        value = dailyNote,
                        onValueChange = { dailyNote = it },
                        placeholder = { Text("Adicione uma nota sobre hoje...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Card de Ofensiva e Lembrete
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${currentHabit.streakDays} dias seguidos 🔥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1D1B20)
                        )
                        if (currentHabit.hasReminder && currentHabit.reminderTime.isNotBlank()) {
                            Text(
                                text = "Lembrete às ${currentHabit.reminderTime}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botão Salvar progresso
                Button(
                    onClick = {
                        viewModel.updateProgress(habitId, currentValue, dailyNote)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(text = "Salvar progresso", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryColor)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Hábito") },
            text = { Text("Deseja realmente excluir este hábito? Esta ação não poderá ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habitId)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}