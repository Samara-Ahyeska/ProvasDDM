package com.example.prova6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova6.data.PantryMock
import com.example.prova6.model.PantryItem
import com.example.prova6.model.PantryLevel
import com.example.prova6.repository.PantryRepository
import com.example.prova6.ui.theme.AlertRed
import com.example.prova6.ui.theme.BadgeBackground
import com.example.prova6.ui.theme.CardBorderColor
import com.example.prova6.ui.theme.CardSurface
import com.example.prova6.ui.theme.DividerSoft
import com.example.prova6.ui.theme.ForestGreen
import com.example.prova6.ui.theme.ListfyTheme
import com.example.prova6.ui.theme.ScreenBackground
import com.example.prova6.ui.theme.TextPrimary
import com.example.prova6.ui.theme.TextSecondary

private val AmberGold = Color(0xFFD89B00)
private val SelectedCardTint = Color(0xFFF3F8F5)
private val SelectedLevelTint = Color(0xFFE7F2EC)

private data class PantryBottomTab(
    val label: String,
    val icon: ImageVector
)

@Composable
fun PantryRoute(
    repository: PantryRepository = remember { PantryRepository() },
    onAddNewClick: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val locations = remember(repository) { repository.getLocations() }
    var selectedLocation by remember { mutableStateOf("Todos") }
    var items by remember(repository) { mutableStateOf(repository.getPantryItems()) }
    var selectedTab by remember { mutableIntStateOf(1) }

    val filteredItems = remember(items, selectedLocation) {
        if (selectedLocation == "Todos") items
        else items.filter { it.location.equals(selectedLocation, ignoreCase = true) }
    }

    PantryScreen(
        locations = locations,
        selectedLocation = selectedLocation,
        items = filteredItems,
        selectedTab = selectedTab,
        onSelectLocation = { selectedLocation = it },
        onAddNewClick = onAddNewClick,
        onToggleExpand = { id ->
            items = items.map { item ->
                if (item.id == id) item.copy(isExpanded = !item.isExpanded) else item
            }
        },
        onSelectLevel = { id, newLevel ->
            items = items.map { item ->
                if (item.id == id) item.copy(level = newLevel) else item
            }
        },
        onSelectTab = { index ->
            selectedTab = index
            onNavigateToTab(index)
        }
    )
}

@Composable
fun PantryScreen(
    locations: List<String>,
    selectedLocation: String,
    items: List<PantryItem>,
    selectedTab: Int,
    onSelectLocation: (String) -> Unit,
    onAddNewClick: () -> Unit,
    onToggleExpand: (String) -> Unit,
    onSelectLevel: (String, PantryLevel) -> Unit,
    onSelectTab: (Int) -> Unit
) {
    val bottomTabs = listOf(
        PantryBottomTab("Início", Icons.Rounded.Home),
        PantryBottomTab("Despensa", Icons.Rounded.Inventory2),
        PantryBottomTab("Lista", Icons.Rounded.FormatListBulleted),
        PantryBottomTab("Métricas", Icons.Rounded.QueryStats)
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
            contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Despensa",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onAddNewClick,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Novo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(locations) { location ->
                        val isSelected = location == selectedLocation
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) ForestGreen else CardSurface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else CardBorderColor
                            ),
                            modifier = Modifier.clickable { onSelectLocation(location) }
                        ) {
                            Text(
                                text = location,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            items(items, key = { it.id }) { item ->
                if (item.isExpanded) {
                    ExpandedPantryCard(
                        item = item,
                        onHeaderClick = { onToggleExpand(item.id) },
                        onSelectLevel = { level -> onSelectLevel(item.id, level) }
                    )
                } else {
                    CompactPantryRow(
                        item = item,
                        onClick = { onToggleExpand(item.id) }
                    )
                }
            }

            item {
                Text(
                    text = "Itens em Baixo ou Esgotado entram na lista\nde compras automaticamente.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandedPantryCard(
    item: PantryItem,
    onHeaderClick: () -> Unit,
    onSelectLevel: (PantryLevel) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = SelectedCardTint,
        border = BorderStroke(1.5.dp, ForestGreen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.location} · ${item.defaultQuantityLabel}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                PantryLevelBadge(level = item.level)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PantryLevel.entries.forEach { level ->
                    val isSelected = item.level == level
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectLevel(level) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SelectedLevelTint else CardSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ForestGreen else CardBorderColor
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LevelDot(level = level)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = level.label,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = level.percentageLabel,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPantryRow(
    item: PantryItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.location,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PantryLevelBadge(level = item.level)
        }

        HorizontalDivider(
            color = DividerSoft,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PantryLevelBadge(level: PantryLevel) {
    Surface(
        shape = CircleShape,
        color = BadgeBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LevelDot(level = level)
            Text(
                text = level.label,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LevelDot(level: PantryLevel) {
    when (level) {
        PantryLevel.FULL -> {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ForestGreen)
            )
        }
        PantryLevel.MEDIUM -> {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AmberGold)
            )
        }
        PantryLevel.LOW -> {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AlertRed)
            )
        }
        PantryLevel.OUT_OF_STOCK -> {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .border(2.dp, TextSecondary, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantryScreenPreview() {
    ListfyTheme {
        PantryScreen(
            locations = PantryMock.locations,
            selectedLocation = "Todos",
            items = PantryMock.initialItems(),
            selectedTab = 1,
            onSelectLocation = {},
            onAddNewClick = {},
            onToggleExpand = {},
            onSelectLevel = { _, _ -> },
            onSelectTab = {}
        )
    }
}