package com.example.prova6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova6.data.ShoppingListMock
import com.example.prova6.model.MarketAisle
import com.example.prova6.model.MeasurementCategory
import com.example.prova6.model.MeasurementUnit
import com.example.prova6.model.ShoppingListItem
import com.example.prova6.model.ShoppingListSummary
import com.example.prova6.model.ShoppingSection
import com.example.prova6.repository.ShoppingListRepository
import com.example.prova6.ui.theme.CardBorderColor
import com.example.prova6.ui.theme.CardSurface
import com.example.prova6.ui.theme.DividerSoft
import com.example.prova6.ui.theme.ForestGreen
import com.example.prova6.ui.theme.ListfyTheme
import com.example.prova6.ui.theme.ScreenBackground
import com.example.prova6.ui.theme.TextPrimary
import com.example.prova6.ui.theme.TextSecondary

private val SectionBandColor = Color(0xFFF1EFE8)
private val SelectedPillTint = Color(0xFFE7F2EC)
private val HandleBarColor = Color(0xFFD6D2C7)

private data class ShoppingBottomTab(
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListRoute(
    repository: ShoppingListRepository = remember { ShoppingListRepository() },
    onNavigateToTab: (Int) -> Unit = {}
) {
    val initialData = remember(repository) { repository.getShoppingListSummary() }
    var summary by remember { mutableStateOf(initialData) }
    var quickNameInput by remember { mutableStateOf("") }
    var isManualModalOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(2) }

    val openModalWithCurrentName: () -> Unit = {
        isManualModalOpen = true
    }

    val addManualItem: (
        name: String,
        aisle: MarketAisle,
        quantityLabel: String,
        estimatedPrice: Double
    ) -> Unit = { name, aisle, quantityLabel, estimatedPrice ->
        val cleanName = name.trim()
        if (cleanName.isNotEmpty()) {
            val newItem = ShoppingListItem(
                id = "manual_${System.currentTimeMillis()}",
                name = cleanName,
                originLabel = "Avulso",
                quantityLabel = quantityLabel,
                estimatedPrice = estimatedPrice
            )

            val targetSectionIndex = summary.sections.indexOfFirst { it.id == aisle.id }
            val updatedSections = if (targetSectionIndex >= 0) {
                summary.sections.mapIndexed { idx, section ->
                    if (idx == targetSectionIndex) {
                        val nextCount = section.totalCount + 1
                        section.copy(
                            title = aisle.sectionHeaderTitle(nextCount),
                            totalCount = nextCount,
                            items = section.items + newItem
                        )
                    } else {
                        section
                    }
                }
            } else {
                summary.sections + ShoppingSection(
                    id = aisle.id,
                    title = aisle.sectionHeaderTitle(1),
                    totalCount = 1,
                    items = listOf(newItem)
                )
            }

            summary = summary.copy(
                totalItemsCount = summary.totalItemsCount + 1,
                estimatedTotal = summary.estimatedTotal + estimatedPrice,
                sections = updatedSections
            )
            quickNameInput = ""
            isManualModalOpen = false
        }
    }

    val toggleItemChecked: (String) -> Unit = { itemId ->
        val updatedSections = summary.sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                }
            )
        }
        summary = summary.copy(sections = updatedSections)
    }

    ShoppingListScreen(
        summary = summary,
        quickNameInput = quickNameInput,
        selectedTab = selectedTab,
        onQuickNameChange = { quickNameInput = it },
        onOpenManualModal = openModalWithCurrentName,
        onToggleItemChecked = toggleItemChecked,
        onSelectTab = { index ->
            selectedTab = index
            onNavigateToTab(index)
        }
    )

    if (isManualModalOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isManualModalOpen = false },
            sheetState = sheetState,
            containerColor = CardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HandleBarColor)
                )
            }
        ) {
            ManualItemBottomSheetContent(
                initialName = quickNameInput,
                onCancel = { isManualModalOpen = false },
                onConfirm = { name, aisle, quantityLabel, price ->
                    addManualItem(name, aisle, quantityLabel, price)
                }
            )
        }
    }
}

@Composable
fun ShoppingListScreen(
    summary: ShoppingListSummary,
    quickNameInput: String,
    selectedTab: Int,
    onQuickNameChange: (String) -> Unit,
    onOpenManualModal: () -> Unit,
    onToggleItemChecked: (String) -> Unit,
    onSelectTab: (Int) -> Unit
) {
    val bottomTabs = listOf(
        ShoppingBottomTab("Início", Icons.Rounded.Home),
        ShoppingBottomTab("Despensa", Icons.Rounded.Inventory2),
        ShoppingBottomTab("Lista", Icons.Rounded.FormatListBulleted),
        ShoppingBottomTab("Métricas", Icons.Rounded.QueryStats)
    )

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                bottomTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val contentColor = if (isSelected) ForestGreen else TextSecondary
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onSelectTab(index) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = contentColor
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Lista de compras",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalItemsCount} itens · estimativa ${summary.formattedEstimatedTotal()}",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quickNameInput,
                        onValueChange = onQuickNameChange,
                        placeholder = {
                            Text(
                                text = "Adicionar item avulso",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = CardBorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onOpenManualModal,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Adicionar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            summary.sections.forEach { section ->
                item(key = "header_${section.id}") {
                    ShoppingSectionHeader(section = section)
                }

                section.items.forEachIndexed { index, item ->
                    item(key = "item_${item.id}") {
                        ShoppingListItemRow(
                            item = item,
                            onClick = { onToggleItemChecked(item.id) }
                        )
                        if (index < section.items.lastIndex) {
                            HorizontalDivider(
                                color = DividerSoft,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualItemBottomSheetContent(
    initialName: String = "",
    onCancel: () -> Unit,
    onConfirm: (name: String, aisle: MarketAisle, quantityLabel: String, estimatedPrice: Double) -> Unit
) {
    var name by remember(initialName) {
        mutableStateOf(initialName.ifBlank { "Pilha AA" })
    }
    var selectedAisle by remember { mutableStateOf(MarketAisle.all[5]) } // Higiene & Limpeza[cite: 6]
    var selectedCategory by remember { mutableStateOf(MeasurementCategory.QUANTITY) }
    var selectedUnit by remember(selectedCategory) {
        mutableStateOf(selectedCategory.units().first())
    }
    var amount by remember(selectedUnit) {
        mutableDoubleStateOf(selectedUnit.defaultAmount)
    }
    var priceText by remember { mutableStateOf("") }

    val parsedPrice = priceText
        .replace("R$", "", ignoreCase = true)
        .replace(",", ".")
        .trim()
        .toDoubleOrNull()
        ?.coerceAtLeast(0.0) ?: 0.0

    val decrementAmount = {
        val next = amount - selectedUnit.step
        val minAllowed = if (selectedUnit.allowDecimal) selectedUnit.step else 1.0
        amount = next.coerceAtLeast(minAllowed)
    }

    val incrementAmount = {
        amount += selectedUnit.step
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Item avulso",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancelar",
                    color = ForestGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Nome",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Corredor do mercado",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarketAisle.all.forEach { aisle ->
                val isSelected = aisle.id == selectedAisle.id
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) ForestGreen else CardSurface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) ForestGreen else CardBorderColor
                    ),
                    modifier = Modifier.clickable { selectedAisle = aisle }
                ) {
                    Text(
                        text = aisle.label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tipo de medida",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeasurementCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) SelectedPillTint else CardSurface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ForestGreen else CardBorderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category.label,
                        color = if (isSelected) ForestGreen else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCategory.units().forEach { unit ->
                val isSelected = unit.symbol == selectedUnit.symbol
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) ForestGreen else ScreenBackground,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) ForestGreen else CardBorderColor
                    ),
                    modifier = Modifier.clickable { selectedUnit = unit }
                ) {
                    Text(
                        text = "${unit.fullName} (${unit.symbol})",
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = selectedUnit.hint,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = "Quantidade (${selectedUnit.symbol})",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StepperButton(
                        icon = Icons.Rounded.Remove,
                        contentDescription = "Diminuir",
                        onClick = decrementAmount
                    )
                    Text(
                        text = selectedUnit.formatAmount(amount),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    StepperButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Aumentar",
                        onClick = incrementAmount
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Preço estimado",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    placeholder = {
                        Text(
                            text = "R$ 0,00 (opcional)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                onConfirm(
                    name,
                    selectedAisle,
                    selectedUnit.formatQuantityLabel(amount),
                    parsedPrice
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Adicionar à lista",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Avulso: não entra na despensa e some da lista ao encerrar as compras.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShoppingSectionHeader(section: ShoppingSection) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBandColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = section.title,
            color = ForestGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = if (item.isChecked) TextSecondary else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.originLabel,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.quantityLabel,
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.formattedPrice(),
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualItemBottomSheetPreview() {
    ListfyTheme {
        Surface(color = CardSurface) {
            ManualItemBottomSheetContent(
                onCancel = {},
                onConfirm = { _, _, _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShoppingListScreenPreview() {
    ListfyTheme {
        ShoppingListScreen(
            summary = ShoppingListMock.initialSummary(),
            quickNameInput = "",
            selectedTab = 2,
            onQuickNameChange = {},
            onOpenManualModal = {},
            onToggleItemChecked = {},
            onSelectTab = {}
        )
    }
}