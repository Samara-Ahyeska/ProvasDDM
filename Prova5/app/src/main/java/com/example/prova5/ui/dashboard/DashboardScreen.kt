package com.example.prova5.ui.dashboard

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prova5.data.model.ExpenseCategory
import com.example.prova5.data.model.ExpenseItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EmeraldDark = Color(0xFF136F48)
private val EmeraldSubCard = Color(0xFF287D59)
private val EmeraldSoftBg = Color(0xFFE3F2EB)
private val ScreenBg = Color(0xFFF6F7F8)
private val CardBorder = Color(0xFFE7EAEF)
private val TextDark = Color(0xFF1A1C1E)
private val TextMuted = Color(0xFF6C757D)

private val StatusGreen = Color(0xFF1E9E5A)
private val StatusGreenBg = Color(0xFFE6F6EC)
private val StatusYellow = Color(0xFFE59800)
private val StatusYellowBg = Color(0xFFFFF6DF)
private val StatusRed = Color(0xFFD93025)
private val StatusRedBg = Color(0xFFFCE8E6)

private const val PROFILE_PREFS_NAME = "user_profile_prefs"

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String,
    userEmail: String,
    onLogout: () -> Unit,
    onUpdateDisplayName: (String, (Result<Unit>) -> Unit) -> Unit = { _, callback ->
        callback(Result.success(Unit))
    },
    onChangePasswordAndLogout: (String, String, (Result<Unit>) -> Unit) -> Unit = { _, _, callback ->
        callback(Result.success(Unit))
    }
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val nicknameKey = remember(userEmail, userName) {
        val accountId = userEmail.trim().lowercase(Locale.ROOT).ifBlank { userName.trim() }
        "nickname_$accountId"
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cycleConfig by viewModel.cycleConfig.collectAsStateWithLifecycle()
    val cycleStartMillis = remember(cycleConfig, uiState) {
        viewModel.getCycleStartMillis()
    }

    var currentUserName by rememberSaveable(userName) { mutableStateOf(userName) }
    var currentNickname by rememberSaveable(nicknameKey) {
        mutableStateOf(prefs.getString(nicknameKey, "").orEmpty())
    }

    LaunchedEffect(userName) {
        currentUserName = userName
    }

    LaunchedEffect(nicknameKey) {
        currentNickname = prefs.getString(nicknameKey, "").orEmpty()
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isAddingExpense by rememberSaveable { mutableStateOf(false) }
    var isAddingBudget by rememberSaveable { mutableStateOf(false) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var editingCategoryCard by remember { mutableStateOf<CategoryCardUi?>(null) }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }

    val initialLetter = remember(currentNickname, currentUserName, userEmail) {
        val base = currentNickname.ifBlank { currentUserName.ifBlank { userEmail } }.trim()
        base.firstOrNull()?.uppercase() ?: "S"
    }

    val greetingText = remember(currentNickname, currentUserName) {
        val displayName = currentNickname.trim().ifBlank {
            currentUserName.trim().substringBefore(" ")
        }
        if (displayName.isNotBlank()) "Olá, $displayName!" else "Olá!"
    }

    if (isAddingExpense) {
        AddExpenseScreen(
            availableCategories = viewModel.categoriesList.value,
            onBack = { isAddingExpense = false },
            onSave = { amount, cat, desc, pay, details, timestamp ->
                viewModel.addExpense(amount, cat, desc, pay, details, timestamp)
                isAddingExpense = false
            }
        )
    } else if (isAddingBudget) {
        AddBudgetScreen(
            onBack = { isAddingBudget = false },
            onSave = { category, limit ->
                viewModel.updateCategoryBudget(category, limit)
                isAddingBudget = false
            }
        )
    } else if (editingCategoryCard != null) {
        EditBudgetScreen(
            category = editingCategoryCard!!.category,
            currentLimit = editingCategoryCard!!.limit,
            onBack = { editingCategoryCard = null },
            onSave = { category, limit ->
                viewModel.updateCategoryBudget(category, limit)
                editingCategoryCard = null
            },
            onDelete = { category ->
                viewModel.deleteCategoryBudget(category)
                editingCategoryCard = null
            }
        )
    } else if (isEditingProfile) {
        EditProfileScreen(
            currentName = currentUserName,
            currentNickname = currentNickname,
            userEmail = userEmail,
            cycleConfig = cycleConfig,
            cycleStartMillis = cycleStartMillis,
            onCycleModeChange = { viewModel.setCycleMode(it) },
            onAnchorDateChange = { viewModel.setAnchorDate(it) },
            onResetCycleNow = { viewModel.resetCycleNow() },
            onBack = { isEditingProfile = false },
            onSaveProfile = { newName, newNickname, currentPassword, newPassword, onError ->
                val trimmedNickname = newNickname.trim()
                currentNickname = trimmedNickname
                prefs.edit().putString(nicknameKey, trimmedNickname).apply()

                val applyNameAndClose = {
                    currentUserName = newName
                    onUpdateDisplayName(newName) { nameResult ->
                        nameResult
                            .onSuccess {
                                if (newPassword != null) {
                                    onChangePasswordAndLogout(currentPassword, newPassword) { passResult ->
                                        passResult.onFailure { err ->
                                            onError(err.message ?: "Erro ao alterar a senha.")
                                        }
                                    }
                                } else {
                                    isEditingProfile = false
                                }
                            }
                            .onFailure { err ->
                                onError(err.message ?: "Não foi possível atualizar o nome.")
                            }
                    }
                }

                if (newPassword != null && newName == currentUserName) {
                    onChangePasswordAndLogout(currentPassword, newPassword) { passResult ->
                        passResult.onFailure { err ->
                            onError(err.message ?: "Erro ao alterar a senha.")
                        }
                    }
                } else {
                    applyNameAndClose()
                }
            },
            onLogout = {
                isEditingProfile = false
                onLogout()
            }
        )
    } else {
        Scaffold(
            containerColor = ScreenBg,
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { isAddingExpense = true },
                        shape = CircleShape,
                        containerColor = EmeraldDark,
                        contentColor = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar gasto",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            bottomBar = {
                DashboardBottomBar(
                    selectedTab = selectedTab,
                    onSelectTab = { tabIndex ->
                        selectedTab = tabIndex
                        if (tabIndex != 1) {
                            selectedCategoryFilter = null
                        }
                    }
                )
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> PainelTabContent(
                    uiState = uiState,
                    greetingText = greetingText,
                    initialLetter = initialLetter,
                    onAvatarClick = { isEditingProfile = true },
                    onCategoryClick = { category ->
                        selectedCategoryFilter = category
                        selectedTab = 1
                    },
                    modifier = Modifier.padding(innerPadding)
                )
                1 -> HistoricoTabContent(
                    expenses = uiState.expenses,
                    categoryFilter = selectedCategoryFilter,
                    onClearFilter = { selectedCategoryFilter = null },
                    modifier = Modifier.padding(innerPadding)
                )
                2 -> TetosTabContent(
                    categories = uiState.budgetedCategories,
                    onCategoryItemClick = { cardUi ->
                        editingCategoryCard = cardUi
                    },
                    onAddNewCategory = { isAddingBudget = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PainelTabContent(
    uiState: DashboardUiState,
    greetingText: String,
    initialLetter: String,
    onAvatarClick: () -> Unit,
    onCategoryClick: (ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greetingText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Painel de gastos · ${uiState.monthYearLabel}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoftBg)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialLetter,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }

        item {
            GlobalSummaryCard(
                totalSpent = uiState.totalSpent,
                totalBudget = uiState.totalBudget,
                remainingBalance = uiState.remainingBalance
            )
        }

        items(uiState.budgetedCategories, key = { it.category.name }) { item ->
            CategoryProgressCard(
                item = item,
                onClick = { onCategoryClick(item.category) }
            )
        }
    }
}

@Composable
private fun GlobalSummaryCard(
    totalSpent: Double,
    totalBudget: Double,
    remainingBalance: Double
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = EmeraldDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total gasto no mês",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(totalSpent),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SubMetricBox(
                    label = "Teto total",
                    value = formatCurrency(totalBudget),
                    modifier = Modifier.weight(1f)
                )
                SubMetricBox(
                    label = "Saldo disponível",
                    value = formatCurrency(remainingBalance),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SubMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = EmeraldSubCard,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CategoryProgressCard(
    item: CategoryCardUi,
    onClick: () -> Unit
) {
    val (accentColor, badgeBg, iconBg) = when (item.alertLevel) {
        AlertLevel.GREEN -> Triple(StatusGreen, StatusGreenBg, Color(0xFFEEF7F2))
        AlertLevel.YELLOW -> Triple(StatusYellow, StatusYellowBg, Color(0xFFFFF8E6))
        AlertLevel.RED -> Triple(StatusRed, StatusRedBg, Color(0xFFFDECEC))
    }

    val statusText = when {
        item.spent > item.limit ->
            "Teto estourado em ${formatCurrency(item.spent - item.limit)}"
        item.alertLevel == AlertLevel.RED ->
            "Limite crítico: restam apenas ${formatCurrency((item.limit - item.spent).coerceAtLeast(0.0))}"
        item.alertLevel == AlertLevel.YELLOW ->
            "Atenção: restam apenas ${formatCurrency((item.limit - item.spent).coerceAtLeast(0.0))}"
        else ->
            "Restam ${formatCurrency((item.limit - item.spent).coerceAtLeast(0.0))}"
    }

    val statusTextColor = when (item.alertLevel) {
        AlertLevel.RED -> StatusRed
        AlertLevel.YELLOW -> TextDark
        AlertLevel.GREEN -> TextMuted
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.category.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.category.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatCurrency(item.spent)} de ${formatCurrency(item.limit)}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = badgeBg
                ) {
                    Text(
                        text = "${item.percentage}%",
                        color = if (item.alertLevel == AlertLevel.YELLOW) TextDark else accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E5E9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = if (item.alertLevel != AlertLevel.GREEN) FontWeight.Medium else FontWeight.Normal,
                color = statusTextColor
            )
        }
    }
}

@Composable
private fun DashboardBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val items = listOf(
        Triple("Painel", "🏠", 0),
        Triple("Histórico", "🧾", 1),
        Triple("Tetos", "🎯", 2)
    )

    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (label, emoji, index) ->
                    val isSelected = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) EmeraldSoftBg else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextDark else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoricoTabContent(
    expenses: List<ExpenseItem>,
    categoryFilter: ExpenseCategory?,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredExpenses = remember(expenses, categoryFilter) {
        if (categoryFilter != null) {
            expenses.filter { it.category.name == categoryFilter.name }
        } else {
            expenses
        }
    }

    var selectedExpenseForDetails by remember { mutableStateOf<ExpenseItem?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (categoryFilter != null) "Histórico: ${categoryFilter.title}" else "Histórico de lançamentos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (categoryFilter != null) {
                    TextButton(onClick = onClearFilter) {
                        Text(text = "Ver todos", color = EmeraldDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum lançamento encontrado nesta categoria.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(expense.timestamp))
                val detailText = if (expense.paymentDetails.isNotBlank()) {
                    "${expense.category.title} · ${expense.paymentMethod.label} (${expense.paymentDetails}) · $dateFormatted"
                } else {
                    "${expense.category.title} · ${expense.paymentMethod.label} · $dateFormatted"
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedExpenseForDetails = expense }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = expense.category.icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.description,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                            Text(
                                text = detailText,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatCurrency(expense.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }

    if (selectedExpenseForDetails != null) {
        ExpenseDetailsDialog(
            expense = selectedExpenseForDetails!!,
            onDismiss = { selectedExpenseForDetails = null }
        )
    }
}

@Composable
private fun ExpenseDetailsDialog(
    expense: ExpenseItem,
    onDismiss: () -> Unit
) {
    val fullDateFormatted = remember(expense.timestamp) {
        SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date(expense.timestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = expense.category.icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Detalhes da compra",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRowItem(label = "Descrição", value = expense.description)
                DetailRowItem(label = "Valor", value = formatCurrency(expense.amount), valueColor = EmeraldDark)
                DetailRowItem(label = "Categoria", value = expense.category.title)
                DetailRowItem(
                    label = "Pagamento",
                    value = if (expense.paymentDetails.isNotBlank()) {
                        "${expense.paymentMethod.label} (${expense.paymentDetails})"
                    } else {
                        expense.paymentMethod.label
                    }
                )
                DetailRowItem(label = "Data", value = fullDateFormatted)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark,
                    contentColor = Color.White
                )
            ) {
                Text("Fechar", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    valueColor: Color = TextDark
) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun TetosTabContent(
    categories: List<CategoryCardUi>,
    onCategoryItemClick: (CategoryCardUi) -> Unit,
    onAddNewCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Tetos por categoria",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(categories, key = { it.category.name }) { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryItemClick(item) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF7F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.category.icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.category.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Teto mensal: ${formatCurrency(item.limit)}",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, EmeraldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { onAddNewCategory() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar",
                        tint = EmeraldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adicionar outro teto",
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(value)
}