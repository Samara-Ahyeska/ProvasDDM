package com.example.prova4.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova4.data.model.Habit
import com.example.prova4.data.model.HabitPeriod
import com.example.prova4.data.model.HabitType
import com.example.prova4.ui.home.HomeViewModel
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

    var nameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Saúde") }
    var selectedType by remember { mutableStateOf(HabitType.SIMPLES) }
    var targetValueInput by remember { mutableStateOf("2000") }
    var selectedPeriod by remember { mutableStateOf(HabitPeriod.MANHA) }
    var hasReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("10:00") }

    LaunchedEffect(habitId) {
        if (!habitId.isNullOrBlank()) {
            viewModel.loadHabit(habitId)
        }
    }

    LaunchedEffect(selectedHabit) {
        selectedHabit?.let {
            nameInput = it.title
            selectedCategory = it.category.ifBlank { "Saúde" }
            selectedType = it.type
            targetValueInput = if (it.targetValue > 0) it.targetValue.toString() else "2000"
            selectedPeriod = it.periods.firstOrNull() ?: HabitPeriod.MANHA
            hasReminder = it.hasReminder
            reminderTime = it.reminderTime.ifBlank { "10:00" }
        }
    }

    val categories = categoriesState.categories.ifEmpty { listOf("Saúde", "Estudos", "Trabalho", "Mindfulness") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar", color = Color.Gray, fontSize = 16.sp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (habitId.isNullOrBlank()) "Novo hábito" else "Editar hábito",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )

            // Campo Nome
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nome", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Seleção de Categoria
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoria", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.filter { it != "Todas" }.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1D1B20)
                            )
                        )
                    }
                }
            }

            // Tipo de Hábito
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de hábito", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) primaryColor else Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) Color(0xFFE6F4F1) else Color.White,
                                contentColor = Color(0xFF1D1B20)
                            )
                        ) {
                            Text(label, fontSize = 12.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // Meta Diária (Exibido se for Numérico ou condicional)
            if (selectedType == HabitType.NUMERICO) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Meta diária (ml / un)", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = targetValueInput,
                        onValueChange = { targetValueInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // Período
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Período", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(HabitPeriod.MANHA to "Manhã", HabitPeriod.TARDE to "Tarde", HabitPeriod.NOITE to "Noite")
                    periods.forEach { (period, label) ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1D1B20)
                            )
                        )
                    }
                }
            }

            // Lembrete Card com Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Lembrete", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Todos os dias às $reminderTime", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Criar / Salvar Hábito
            Button(
                onClick = {
                    val habitToSave = selectedHabit?.copy(
                        title = nameInput.ifBlank { "Novo Hábito" },
                        category = selectedCategory,
                        type = selectedType,
                        targetValue = targetValueInput.toIntOrNull() ?: 0,
                        periods = listOf(selectedPeriod),
                        hasReminder = hasReminder,
                        reminderTime = reminderTime
                    ) ?: Habit(
                        id = UUID.randomUUID().toString(),
                        title = nameInput.ifBlank { "Novo Hábito" },
                        category = selectedCategory,
                        type = selectedType,
                        targetValue = targetValueInput.toIntOrNull() ?: 0,
                        periods = listOf(selectedPeriod),
                        hasReminder = hasReminder,
                        reminderTime = reminderTime
                    )

                    viewModel.saveHabit(habitToSave)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    text = if (habitId.isNullOrBlank()) "Criar hábito" else "Salvar alterações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}