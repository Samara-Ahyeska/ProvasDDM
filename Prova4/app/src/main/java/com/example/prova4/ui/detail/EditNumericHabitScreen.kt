package com.example.prova4.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova4.data.model.HabitPeriod
import com.example.prova4.ui.home.HomeViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumericHabitScreen(
    habitId: String,
    viewModel: DetailViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    val categoriesState by homeViewModel.uiState.collectAsState()
    val primaryColor = Color(0xFF0F766E)
    val backgroundColor = Color(0xFFF7F5EE)

    val frequencyOptions = listOf("Todo dia", "Dias úteis", "Fim de semana", "Somente hoje")

    val habit = selectedHabit
    val isReadOnly = habit?.isCompletedToday == true

    var currentValue by remember { mutableDoubleStateOf(0.0) }
    var currentUnit by remember { mutableStateOf("ml") }
    var rawTargetValue by remember { mutableDoubleStateOf(2000.0) }
    var rawTargetUnit by remember { mutableStateOf("ml") }

    var dailyNoteInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Saúde") }

    // Suporte a Múltiplos Períodos
    val selectedPeriods = remember { mutableStateListOf(HabitPeriod.MANHA) }

    var selectedFrequency by remember { mutableStateOf("Todo dia") }
    var hasReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("10:00") }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showManualValueDialog by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    LaunchedEffect(selectedHabit) {
        selectedHabit?.let { h ->
            currentValue = h.currentValue.toDouble()
            currentUnit = h.unit.ifBlank { "ml" }
            rawTargetValue = if (h.targetValue > 0) h.targetValue.toDouble() else 2000.0
            rawTargetUnit = h.unit.ifBlank { "ml" }
            dailyNoteInput = h.dailyNote
            selectedCategory = h.category.ifBlank { "Saúde" }

            selectedPeriods.clear()
            if (h.periods.isNotEmpty()) {
                selectedPeriods.addAll(h.periods)
            } else {
                selectedPeriods.add(HabitPeriod.MANHA)
            }

            selectedFrequency = h.frequency.ifBlank { "Todo dia" }
            hasReminder = h.hasReminder
            reminderTime = h.reminderTime.ifBlank { "10:00" }
        }
    }

    val incrementStep = remember(currentUnit) {
        when (currentUnit.lowercase()) {
            "ml" -> 250.0
            "l" -> 0.25
            "m" -> 5.0
            "km" -> 1.0
            "g", "gr" -> 100.0
            "kg" -> 1.0
            else -> 1.0
        }
    }

    fun adjustUnitAndValue(newValue: Double, unit: String): Pair<Double, String> {
        var valOut = newValue.coerceAtLeast(0.0)
        var unitOut = unit

        when (unitOut.lowercase()) {
            "ml" -> {
                if (valOut >= 1000) {
                    valOut /= 1000
                    unitOut = "L"
                }
            }
            "m" -> {
                if (valOut >= 1000) {
                    valOut /= 1000
                    unitOut = "km"
                }
            }
            "g", "gr" -> {
                if (valOut >= 1000) {
                    valOut /= 1000
                    unitOut = "kg"
                }
            }
        }
        return Pair(valOut, unitOut)
    }

    val displayTargetValue = remember(rawTargetValue, rawTargetUnit, currentUnit) {
        val rawU = rawTargetUnit.lowercase()
        val currU = currentUnit.lowercase()

        if ((rawU == "ml" && currU == "l") || (rawU == "m" && currU == "km") || ((rawU == "g" || rawU == "gr") && currU == "kg")) {
            rawTargetValue / 1000.0
        } else if ((rawU == "l" && currU == "ml") || (rawU == "km" && currU == "m") || (rawU == "kg" && (currU == "g" || currU == "gr"))) {
            rawTargetValue * 1000.0
        } else {
            rawTargetValue
        }
    }

    val streakDays = remember(habit) {
        if (habit == null) 0 else homeViewModel.calculateStreak(habit.completedDates, LocalDate.now())
    }

    val completionPercentage30Days = remember(habit) {
        if (habit == null) 0 else {
            val today = LocalDate.now()
            val thirtyDaysAgo = today.minusDays(29)
            val completedLast30Days = habit.completedDates.count { date ->
                !date.isBefore(thirtyDaysAgo) && !date.isAfter(today)
            }
            ((completedLast30Days / 30.0) * 100).toInt()
        }
    }

    val categories = remember(categoriesState.categories) {
        categoriesState.categories.filter { it != "Todas" }.ifEmpty { listOf("Saúde", "Estudos", "Trabalho", "Mindfulness") }
    }

    val progressFraction = remember(currentValue, displayTargetValue) {
        if (displayTargetValue > 0) (currentValue / displayTargetValue).toFloat().coerceIn(0f, 1f) else 0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Voltar", color = Color(0xFF4B5563), fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // Cabeçalho Hábito
            Column {
                Text(
                    text = habit?.title ?: "Beber água",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                val targetStr = if (displayTargetValue % 1.0 == 0.0) displayTargetValue.toInt().toString() else String.format("%.1f", displayTargetValue)
                Text(
                    text = "Meta: $targetStr $currentUnit · $selectedCategory",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Gráfico Circular de Progresso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = !isReadOnly) { showManualValueDialog = true },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(170.dp),
                    color = Color(0xFFE5E7EB),
                    strokeWidth = 14.dp
                )
                CircularProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.size(170.dp),
                    color = if (isReadOnly) Color(0xFF9CA3AF) else primaryColor,
                    strokeWidth = 14.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayVal = if (currentValue % 1.0 == 0.0) currentValue.toInt().toString() else String.format("%.1f", currentValue)
                    val targetStr = if (displayTargetValue % 1.0 == 0.0) displayTargetValue.toInt().toString() else String.format("%.1f", displayTargetValue)

                    Text(
                        text = displayVal,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "de $targetStr $currentUnit",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Controles de Incremento e Decremento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val newV = (currentValue - incrementStep).coerceAtLeast(0.0)
                        val (adjV, adjU) = adjustUnitAndValue(newV, currentUnit)
                        currentValue = adjV
                        currentUnit = adjU
                    },
                    enabled = !isReadOnly,
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("-", fontSize = 24.sp, color = if (isReadOnly) Color.Gray else Color(0xFF374151), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val newV = currentValue + incrementStep
                        val (adjV, adjU) = adjustUnitAndValue(newV, currentUnit)
                        currentValue = adjV
                        currentUnit = adjU
                    },
                    enabled = !isReadOnly,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = Color(0xFF9CA3AF)
                    )
                ) {
                    val stepText = if (incrementStep % 1.0 == 0.0) incrementStep.toInt().toString() else incrementStep.toString()
                    Text(
                        text = "+ $stepText $currentUnit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Nota do dia
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Nota do dia", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = dailyNoteInput,
                    onValueChange = { dailyNoteInput = it },
                    enabled = !isReadOnly,
                    placeholder = { Text("Mais fácil com a garrafa na mesa.", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // Cards de Estatísticas SEPARADOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$streakDays",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "dias seguidos",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$completionPercentage30Days%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "últimos 30 dias",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Seleção de Categoria
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Categoria", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier.clickable(enabled = !isReadOnly) { selectedCategory = cat },
                            color = if (isSelected) (if (isReadOnly) Color.Gray else primaryColor) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Frequência
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Frequência", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frequencyOptions.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        Surface(
                            modifier = Modifier.clickable(enabled = !isReadOnly) { selectedFrequency = freq },
                            color = if (isSelected) (if (isReadOnly) Color.Gray else primaryColor) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text(
                                text = freq,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Período (SELEÇÃO MÚLTIPLA)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Período (selecione um ou mais)", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val periods = listOf(
                        HabitPeriod.MANHA to "Manhã",
                        HabitPeriod.TARDE to "Tarde",
                        HabitPeriod.NOITE to "Noite"
                    )
                    periods.forEach { pair: Pair<HabitPeriod, String> ->
                        val period = pair.first
                        val label = pair.second
                        val isSelected = selectedPeriods.contains(period)
                        Surface(
                            modifier = Modifier.clickable(enabled = !isReadOnly) {
                                if (isSelected) {
                                    if (selectedPeriods.size > 1) {
                                        selectedPeriods.remove(period)
                                    }
                                } else {
                                    selectedPeriods.add(period)
                                }
                            },
                            color = if (isSelected) (if (isReadOnly) Color.Gray else primaryColor) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Lembrete
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isReadOnly) { showTimePickerDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Lembrete", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
                        Text(text = "Horário: $reminderTime", fontSize = 13.sp, color = if (isReadOnly) Color.Gray else primaryColor)
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { if (!isReadOnly) hasReminder = it },
                        enabled = !isReadOnly,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Salvar Progresso
            Button(
                onClick = {
                    if (!isReadOnly) {
                        selectedHabit?.let { habitToSave ->
                            val isCompleted = currentValue >= displayTargetValue
                            viewModel.saveHabit(
                                habitToSave.copy(
                                    currentValue = currentValue.toInt(),
                                    unit = currentUnit,
                                    dailyNote = dailyNoteInput,
                                    category = selectedCategory,
                                    periods = selectedPeriods.toList(),
                                    frequency = selectedFrequency,
                                    hasReminder = hasReminder,
                                    reminderTime = reminderTime,
                                    isCompletedToday = isCompleted
                                )
                            )
                            if (isCompleted) {
                                homeViewModel.toggleHabitCompletion(habitToSave.id)
                            }
                        }
                        onNavigateBack()
                    }
                },
                enabled = !isReadOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = Color(0xFF9CA3AF)
                )
            ) {
                Text(
                    text = if (isReadOnly) "Hábito concluído" else "Salvar progresso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Botão Excluir Hábito
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFB91C1C)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C))
            ) {
                Text(
                    text = "Excluir hábito",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showManualValueDialog && !isReadOnly) {
        var inputVal by remember { mutableStateOf(currentValue.toString()) }
        AlertDialog(
            onDismissRequest = { showManualValueDialog = false },
            title = { Text("Atualizar valor manualmente", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Digite a quantidade atual em $currentUnit:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputVal.replace(',', '.').toDoubleOrNull() ?: currentValue
                        val (adjV, adjU) = adjustUnitAndValue(parsed, currentUnit)
                        currentValue = adjV
                        currentUnit = adjU
                        showManualValueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualValueDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir hábito", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir este hábito?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habitId)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showTimePickerDialog && !isReadOnly) {
        CustomTimePickerDialog(
            currentValue = reminderTime,
            primaryColor = primaryColor,
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { formattedTime: String ->
                reminderTime = formattedTime
                showTimePickerDialog = false
            }
        )
    }
}