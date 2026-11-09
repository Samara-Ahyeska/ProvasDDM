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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimerHabitScreen(
    habitId: String,
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFullEdit: (String) -> Unit
) {
    val habit by viewModel.selectedHabit.collectAsState()

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val timerColor = Color(0xFF8B5CF6) // Cor roxa característica do timer/meditação

    var totalDurationSeconds by remember { mutableStateOf(15 * 60) } // Padrão 15 min
    var timeElapsedSeconds by remember { mutableStateOf(0) } // Tempo já decorrido
    var isRunning by remember { mutableStateOf(false) }
    var isCompletedToday by remember { mutableStateOf(false) }
    var dailyNote by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Inicializa os valores com base no hábito salvo
    LaunchedEffect(habit) {
        habit?.let {
            val goalSeconds = if (it.durationMinutes > 0) it.durationMinutes * 60 else 15 * 60
            totalDurationSeconds = goalSeconds
            timeElapsedSeconds = it.currentValue.coerceIn(0, goalSeconds)

            // Se a nota salva for exatamente a mensagem padrão ou vazia, limpa para exibir o placeholder
            dailyNote = if (it.dailyNote.isBlank() || it.dailyNote == "Nenhuma nota adicionada") "" else it.dailyNote

            isCompletedToday = it.isCompletedToday
        }
    }

    // Lógica do Timer (Só roda se estiver ativo E se o hábito NÃO estiver concluído)
    LaunchedEffect(isRunning, timeElapsedSeconds, isCompletedToday) {
        if (isRunning && !isCompletedToday && timeElapsedSeconds < totalDurationSeconds) {
            delay(1000L)
            timeElapsedSeconds += 1
            if (timeElapsedSeconds >= totalDurationSeconds) {
                isRunning = false
            }
        }
    }

    val timeRemainingSeconds = (totalDurationSeconds - timeElapsedSeconds).coerceAtLeast(0)
    val progressFraction = if (totalDurationSeconds > 0) {
        (timeElapsedSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val formatTime: (Int) -> String = { totalSecs ->
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        String.format("%02d:%02d", mins, secs)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = habit?.title ?: "Timer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1D1B20)
                        )
                        habit?.let {
                            Text(
                                text = "Meta: ${it.durationMinutes} minutos · ${it.category}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF1D1B20))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToFullEdit(habitId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = primaryColor)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFFBFBFA)
    ) { padding ->
        habit?.let { currentHabit ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Gráfico Circular do Timer
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
                        color = if (isCompletedToday) primaryColor else timerColor,
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatTime(timeElapsedSeconds),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = if (isCompletedToday) "Hábito concluído" else "restam ${formatTime(timeRemainingSeconds)}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Botões de Controle do Timer (Exibidos apenas se não estiver concluído)
                if (!isCompletedToday) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "Reiniciar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = timerColor)
                        ) {
                            Text(
                                text = if (isRunning) "Pausar" else "Iniciar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Seção "Como foi?" (Nota sobre a sessão)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Como foi?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = dailyNote,
                        onValueChange = { dailyNote = it },
                        placeholder = { Text("Escreva uma nota sobre a sessão") },
                        enabled = !isCompletedToday,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = timerColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botões inferiores baseados no status de conclusão
                if (!isCompletedToday) {
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            val finalNote = if (dailyNote.isBlank()) "Nenhuma nota adicionada" else dailyNote
                            viewModel.updateProgress(habitId, timeElapsedSeconds, finalNote)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, timerColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Salvar progresso",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = timerColor
                        )
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            val finalNote = if (dailyNote.isBlank()) "Nenhuma nota adicionada" else dailyNote
                            viewModel.updateProgress(habitId, timeElapsedSeconds, finalNote)
                            viewModel.toggleHabitCompletion(habitId)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(
                            text = "Concluir hábito",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.toggleHabitCompletion(habitId)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = "Retornar para pendente",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryColor)
        }
    }

    // Diálogo de Confirmação para Reiniciar o Timer
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar Timer") },
            text = { Text("Tem certeza de que deseja reiniciar o timer? O progresso atual da sessão será zerado.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isRunning = false
                        timeElapsedSeconds = 0
                        showResetDialog = false
                    }
                ) {
                    Text("Sim, reiniciar", color = timerColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Hábito") },
            text = { Text("Deseja realmente excluir este hábito?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habitId)
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}