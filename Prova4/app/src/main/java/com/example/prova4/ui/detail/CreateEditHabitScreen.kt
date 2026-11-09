package com.example.prova4.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import com.example.prova4.data.model.Habit
import com.example.prova4.data.model.HabitPeriod
import com.example.prova4.data.model.HabitType
import com.example.prova4.ui.home.HomeViewModel
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditHabitScreen(
    habitId: String?,
    viewModel: DetailViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    val categoriesState by homeViewModel.uiState.collectAsState()
    val primaryColor = Color(0xFF0F766E)
    val backgroundColor = Color(0xFFF7F5EE)

    val frequencyOptions = listOf("Todo dia", "Dias úteis", "Fim de semana", "Somente hoje")

    val isEditing = !habitId.isNullOrBlank()
    val currentHabit = selectedHabit
    val isReadOnly = isEditing && currentHabit?.isCompletedToday == true

    // Form State
    var nameInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Saúde") }
    var selectedType by remember { mutableStateOf(HabitType.SIMPLES) }
    var targetValueInput by remember { mutableStateOf("2000") }
    var durationMinutesInput by remember { mutableStateOf("30") }
    var unitInput by remember { mutableStateOf("ml") }

    // Suporte a Múltiplos Períodos
    val selectedPeriods = remember { mutableStateListOf(HabitPeriod.MANHA) }

    var selectedFrequency by remember { mutableStateOf("Todo dia") }
    var hasReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("10:00") }

    val customCategories = remember { mutableStateListOf<String>() }
    var newCategoryInput by remember { mutableStateOf("") }
    var isAddingCategory by remember { mutableStateOf(false) }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        if (!habitId.isNullOrBlank()) {
            viewModel.loadHabit(habitId)
        }
    }

    LaunchedEffect(categoriesState.categories) {
        if (categoriesState.categories.isNotEmpty()) {
            customCategories.clear()
            customCategories.addAll(categoriesState.categories.filter { it != "Todas" })
        } else if (customCategories.isEmpty()) {
            customCategories.addAll(listOf("Saúde", "Estudos", "Trabalho", "Mindfulness"))
        }
    }

    LaunchedEffect(selectedHabit) {
        selectedHabit?.let { habit ->
            nameInput = habit.title
            descriptionInput = habit.description
            selectedCategory = habit.category.ifBlank { "Saúde" }
            selectedType = habit.type
            targetValueInput = if (habit.targetValue > 0) habit.targetValue.toString() else "2000"
            durationMinutesInput = if (habit.durationMinutes > 0) habit.durationMinutes.toString() else "30"
            unitInput = habit.unit.ifBlank { "ml" }

            selectedPeriods.clear()
            if (habit.periods.isNotEmpty()) {
                selectedPeriods.addAll(habit.periods)
            } else {
                selectedPeriods.add(HabitPeriod.MANHA)
            }

            selectedFrequency = habit.frequency.ifBlank { "Todo dia" }
            hasReminder = habit.hasReminder
            reminderTime = habit.reminderTime.ifBlank { "10:00" }
        }
    }

    val streakDays = remember(currentHabit) {
        if (currentHabit == null) 0 else homeViewModel.calculateStreak(currentHabit.completedDates, LocalDate.now())
    }

    val completionPercentage30Days = remember(currentHabit) {
        if (currentHabit == null) 0 else {
            val today = LocalDate.now()
            val thirtyDaysAgo = today.minusDays(29)
            val completedLast30Days = currentHabit.completedDates.count { date ->
                !date.isBefore(thirtyDaysAgo) && !date.isAfter(today)
            }
            ((completedLast30Days / 30.0) * 100).toInt()
        }
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
                actions = { },
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


            Text(
                text = if (isEditing) "Editar hábito" else "Novo hábito",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            // 1. Campo Nome
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Nome", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    enabled = !isReadOnly,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // 2. Campo Descrição
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Descrição", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    enabled = !isReadOnly,
                    placeholder = { Text("Ex: Antes de dormir", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // 3. Seleção de Categorias
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Categoria", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    if (!isEditing && !isReadOnly) {
                        TextButton(onClick = { isAddingCategory = !isAddingCategory }) {
                            Text(
                                text = if (isAddingCategory) "Fechar" else "+ Criar categoria",
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }
                    }
                }

                if (!isEditing && isAddingCategory && !isReadOnly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryInput,
                            onValueChange = { newCategoryInput = it },
                            placeholder = { Text("Nova Categoria", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        Button(
                            onClick = {
                                if (newCategoryInput.isNotBlank()) {
                                    val cat = newCategoryInput.trim()
                                    if (!customCategories.contains(cat)) {
                                        customCategories.add(cat)
                                        homeViewModel.addCategory(cat)
                                    }
                                    selectedCategory = cat
                                    newCategoryInput = ""
                                    isAddingCategory = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            color = if (isSelected) (if (isReadOnly) Color.Gray else primaryColor) else Color.White,
                            shape = RoundedCornerShape(50),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable(enabled = !isReadOnly) { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!isEditing && !isReadOnly) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Excluir Categoria",
                                        tint = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val categoryToRemove = cat
                                                customCategories.remove(categoryToRemove)
                                                homeViewModel.deleteCategory(categoryToRemove)
                                                if (selectedCategory == categoryToRemove) {
                                                    selectedCategory = customCategories.firstOrNull() ?: "Geral"
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Tipo de Hábito
            if (!isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Tipo de hábito", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            HabitType.SIMPLES to "Simples\nSim/Não",
                            HabitType.NUMERICO to "Meta\nNumérico",
                            HabitType.TIMER to "Timer\nCronômetro"
                        )
                        types.forEach { (type, label) ->
                            val isSelected = selectedType == type
                            OutlinedButton(
                                onClick = { selectedType = type },
                                enabled = !isReadOnly,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) primaryColor else Color(0xFFE5E7EB)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFFE6F4F1) else Color.White,
                                    contentColor = Color(0xFF111827)
                                )
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 5. Campos Dinâmicos por Tipo
            if (selectedType == HabitType.NUMERICO) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Meta diária", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = targetValueInput,
                            onValueChange = { input -> if (input.all { it.isDigit() }) targetValueInput = input },
                            enabled = !isReadOnly,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color(0xFFF3F4F6)
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Unidade (ex: ml, km)", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = unitInput,
                            onValueChange = { unitInput = it },
                            enabled = !isReadOnly,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color(0xFFF3F4F6)
                            )
                        )
                    }
                }
            }

            if (selectedType == HabitType.TIMER) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Tempo para cronometrar (minutos)", fontSize = 13.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = durationMinutesInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) durationMinutesInput = input },
                        enabled = !isReadOnly,
                        singleLine = true,
                        placeholder = { Text("Ex: 30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color(0xFFF3F4F6)
                        )
                    )
                }
            }

            // 6. Frequência
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

            // 7. Blocos Exclusivos de Edição
            if (isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            Text(text = "Hoje", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                            Text(
                                text = if (currentHabit?.isCompletedToday == true) "Status do dia: feito" else "Status do dia: pendente",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Surface(
                            color = if (currentHabit?.isCompletedToday == true) primaryColor else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable {
                                currentHabit?.let { homeViewModel.toggleHabitCompletion(it.id) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Status",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

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
            }

            // 8. Período (SELEÇÃO MÚLTIPLA COM TIPAGEM EXPLÍCITA)
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

            // 9. Lembrete Editável
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

            Spacer(modifier = Modifier.height(12.dp))

            // 10. Botão Salvar / Criar Hábito
            Button(
                onClick = {
                    if (!isReadOnly) {
                        val habitToSave = selectedHabit?.copy(
                            title = nameInput.ifBlank { "Novo Hábito" },
                            description = descriptionInput,
                            category = selectedCategory,
                            type = selectedType,
                            targetValue = targetValueInput.toIntOrNull() ?: 0,
                            durationMinutes = durationMinutesInput.toIntOrNull() ?: 0,
                            unit = unitInput,
                            periods = selectedPeriods.toList(),
                            frequency = selectedFrequency,
                            hasReminder = hasReminder,
                            reminderTime = reminderTime
                        ) ?: Habit(
                            id = UUID.randomUUID().toString(),
                            title = nameInput.ifBlank { "Novo Hábito" },
                            description = descriptionInput,
                            category = selectedCategory,
                            type = selectedType,
                            targetValue = targetValueInput.toIntOrNull() ?: 0,
                            durationMinutes = durationMinutesInput.toIntOrNull() ?: 0,
                            unit = unitInput,
                            periods = selectedPeriods.toList(),
                            frequency = selectedFrequency,
                            createdAt = homeViewModel.uiState.value.selectedDate,
                            hasReminder = hasReminder,
                            reminderTime = reminderTime
                        )

                        viewModel.saveHabit(habitToSave)
                        homeViewModel.addCategory(selectedCategory)
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
                    text = if (isReadOnly) "Hábito concluído" else (if (isEditing) "Salvar" else "Criar hábito"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 11. Botão Excluir Hábito
            if (isEditing) {
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
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir hábito", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir este hábito?") },
            confirmButton = {
                Button(
                    onClick = {
                        habitId?.let { id ->
                            viewModel.deleteHabit(id)
                            showDeleteConfirmDialog = false
                            onNavigateBack()
                        }
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

/**
 * Diálogo customizado para configurar Horário com suporte a Formato 24h e 12h (AM/PM)
 */
@Composable
fun CustomTimePickerDialog(
    currentValue: String,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var is24HourFormat by remember { mutableStateOf(true) }
    var hourInput by remember { mutableStateOf("10") }
    var minuteInput by remember { mutableStateOf("00") }
    var selectedAmPm by remember { mutableStateOf("AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    var h = hourInput.toIntOrNull() ?: 0
                    val m = (minuteInput.toIntOrNull() ?: 0).coerceIn(0, 59)

                    val formatted = if (is24HourFormat) {
                        h = h % 24
                        String.format("%02d:%02d", h, m)
                    } else {
                        h = h % 13
                        String.format("%02d:%02d %s", h, m, selectedAmPm)
                    }
                    onTimeSelected(formatted)
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Confirmar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        title = { Text(text = "Definir Lembrete", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = is24HourFormat,
                            onClick = { is24HourFormat = true }
                        )
                        Text(text = "24 Horas", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !is24HourFormat,
                            onClick = { is24HourFormat = false }
                        )
                        Text(text = "12 Horas (AM/PM)", fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                hourInput = input
                            }
                        },
                        label = { Text(if (is24HourFormat) "Hora (0-23)" else "Hora (0-12)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Text(text = ":", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    OutlinedTextField(
                        value = minuteInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                minuteInput = input
                            }
                        },
                        label = { Text("Minuto (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (!is24HourFormat) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("AM", "PM").forEach { period ->
                            val isSelected = selectedAmPm == period
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { selectedAmPm = period },
                                color = if (isSelected) primaryColor else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = period,
                                    color = if (isSelected) Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}