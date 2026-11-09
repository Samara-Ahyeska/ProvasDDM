package com.example.prova4.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import com.example.prova4.data.model.matchesFrequency
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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
    val backgroundColor = Color(0xFFF7F5EE)

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
        val parts = uiState.userName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.size >= 2) {
            "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "SA"
        }
    }

    val allHabits = uiState.pendingHabits + uiState.completedHabits

    val filteredHabits = allHabits.filter { habit ->
        val matchesDate = matchesFrequency(
            frequency = habit.frequency,
            targetDate = selectedDate,
            creationDate = habit.createdAt
        )
        val matchesCategory = selectedCategory == "Todas" || habit.category.equals(selectedCategory, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            "Pendentes" -> !habit.isCompletedToday
            "Concluídas" -> habit.isCompletedToday
            else -> true
        }
        matchesDate && matchesCategory && matchesTab
    }

    val totalCount = filteredHabits.size
    val completedCount = filteredHabits.count { it.isCompletedToday }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val categoriesList = remember(uiState.categories) {
        if ("Todas" in uiState.categories) uiState.categories else listOf("Todas") + uiState.categories
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
        containerColor = backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedHeaderDate,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Olá, ${uiState.userName}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
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
                            Text("‹ Semana anterior", fontSize = 12.sp, color = primaryColor)
                        }

                        TextButton(onClick = { viewModel.nextWeek() }) {
                            Text("Próxima semana ›", fontSize = 12.sp, color = primaryColor)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { dayItem ->
                            Card(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 72.dp)
                                    .clickable { viewModel.selectDate(dayItem.date) },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (dayItem.isSelected) primaryColor else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dayItem.letter,
                                        fontSize = 12.sp,
                                        color = if (dayItem.isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF4B5563),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = dayItem.number,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayItem.isSelected) Color.White else Color(0xFF111827)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFE5E7EB),
                                strokeWidth = 9.dp
                            )
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxSize(),
                                color = primaryColor,
                                strokeWidth = 9.dp
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }

                        Column {
                            Text(
                                text = "$completedCount de $totalCount hábitos feitos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalCount - completedCount > 0)
                                    "Faltam ${totalCount - completedCount} para fechar o dia"
                                else
                                    "Parabéns, dia fechado! 🎉",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoriesList.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedCategory = category },
                            color = if (isSelected) primaryColor else Color.White,
                            shape = RoundedCornerShape(50),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E7EB))
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
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedTab = tab },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF111827) else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            val periods = listOf(
                HabitPeriod.MANHA to "MANHÃ",
                HabitPeriod.TARDE to "TARDE",
                HabitPeriod.NOITE to "NOITE"
            )

            periods.forEach { (periodEnum, periodTitle) ->
                val habitsInPeriod = filteredHabits.filter { it.periods.contains(periodEnum) }

                if (habitsInPeriod.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = periodTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(habitsInPeriod) { habit ->
                        HabitCardItem(
                            habit = habit,
                            primaryColor = primaryColor,
                            onCardClick = { onNavigateToEditHabit(habit.id) },
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
fun HabitCardItem(
    habit: Habit,
    primaryColor: Color,
    onCardClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) Color.White.copy(alpha = 0.85f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (habit.isCompletedToday) primaryColor.copy(alpha = 0.5f) else primaryColor)
                )

                Column {
                    Text(
                        text = habit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.isCompletedToday) Color(0xFF6B7280) else Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${habit.streakDays} dias · ${habit.reminderTime.ifBlank { "10:00" }}",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            IconButton(
                onClick = onCheckClick,
                modifier = Modifier
                    .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF9CA3AF),
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