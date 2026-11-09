package com.example.prova4.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova4.data.model.Habit
import com.example.prova4.data.model.HabitPeriod
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Estrutura de dados para os dias da semana do calendário
data class WeekDayItem(
    val date: LocalDate,
    val letter: String,
    val number: String,
    val isSelected: Boolean
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToEditHabit: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = Color(0xFF0F766E)

    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedTab by remember { mutableStateOf("Todas") }

    val localeBR = remember { Locale("pt", "BR") }
    val selectedDate = uiState.selectedDate

    val formattedHeaderDate = remember(selectedDate) {
        val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, localeBR).replaceFirstChar { it.uppercase() }
        val month = selectedDate.month.getDisplayName(TextStyle.FULL, localeBR)
        "$dayOfWeek, ${selectedDate.dayOfMonth} de $month"
    }

    val weekDays = remember(uiState.weekStartDate, selectedDate) {
        (0..6).map { offset ->
            val date = uiState.weekStartDate.plusDays(offset.toLong())
            val letter = date.dayOfWeek.getDisplayName(TextStyle.NARROW, localeBR).uppercase()
            val number = date.dayOfMonth.toString()
            val isSelected = date == selectedDate
            WeekDayItem(date, letter, number, isSelected)
        }
    }

    val userInitials = remember(uiState.userName) {
        val parts = uiState.userName.trim().split("\\s+".toRegex())
        if (parts.size >= 2) {
            "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "SA"
        }
    }

    val allHabits = uiState.pendingHabits + uiState.completedHabits
    val totalCount = allHabits.size
    val completedCount = uiState.completedHabits.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val filteredHabits = allHabits.filter { habit ->
        val matchesCategory = selectedCategory == "Todas" || habit.category.equals(selectedCategory, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            "Pendentes" -> !habit.isCompletedToday
            "Concluídas" -> habit.isCompletedToday
            else -> true
        }
        matchesCategory && matchesTab
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateHabit,
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Hábito")
            }
        },
        containerColor = Color(0xFFF7F5F0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cabeçalho Dinâmico
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedHeaderDate,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Olá, ${uiState.userName}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Barra de Dias da Semana com navegação de semanas
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.previousWeek() }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Semana Anterior", tint = primaryColor)
                            Text("Semana anterior", fontSize = 12.sp, color = primaryColor)
                        }

                        TextButton(onClick = { viewModel.nextWeek() }) {
                            Text("Próxima semana", fontSize = 12.sp, color = primaryColor)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Próxima Semana", tint = primaryColor)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { dayItem ->
                            Card(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 64.dp)
                                    .clickable { viewModel.selectDate(dayItem.date) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (dayItem.isSelected) primaryColor else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dayItem.letter,
                                        fontSize = 11.sp,
                                        color = if (dayItem.isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = dayItem.number,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayItem.isSelected) Color.White else Color(0xFF1D1B20)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card de Progresso Circular
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(68.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFE5E7EB),
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxSize(),
                                color = primaryColor,
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        }

                        Column {
                            Text(
                                text = "$completedCount de $totalCount hábitos feitos",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalCount - completedCount > 0) "Faltam ${totalCount - completedCount} para fechar o dia" else "Parabéns, dia fechado! 🎉",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Chips de Categorias Dinâmicas
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1D1B20)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.LightGray,
                                selectedBorderColor = primaryColor
                            )
                        )
                    }
                }
            }

            // Abas de Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFECE6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Todas", "Pendentes", "Concluídas").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedTab = tab },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF1D1B20) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Seções por Período
            val periods = listOf(HabitPeriod.MANHA to "MANHÃ", HabitPeriod.TARDE to "TARDE", HabitPeriod.NOITE to "NOITE")

            periods.forEach { (periodEnum, periodTitle) ->
                val habitsInPeriod = filteredHabits.filter { it.periods.contains(periodEnum) }

                if (habitsInPeriod.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = periodTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    items(habitsInPeriod) { habit ->
                        OriginalHabitCardItem(
                            habit = habit,
                            primaryColor = primaryColor,
                            onCardClick = {
                                onNavigateToEditHabit(habit.id)
                            },
                            onCheckClick = { viewModel.toggleHabitCompletion(habit.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun OriginalHabitCardItem(
    habit: Habit,
    primaryColor: Color,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )

                Column {
                    Text(
                        text = habit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${habit.streakDays} dias · ${habit.reminderTime.ifBlank { "Diário" }}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (habit.isCompletedToday) primaryColor else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                if (habit.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Concluir",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.LightGray,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}