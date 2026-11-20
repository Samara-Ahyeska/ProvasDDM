package com.example.prova6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.prova6.data.Overview
import com.example.prova6.model.OverviewSummary
import com.example.prova6.model.RestockItem
import com.example.prova6.model.StockStatus
import com.example.prova6.repository.OverviewRepository
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

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun OverviewRoute(
    repository: OverviewRepository = remember { OverviewRepository() },
    onStartSupermarketMode: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val summary = remember(repository) { repository.getOverviewSummary() }
    var selectedTab by remember { mutableIntStateOf(0) }

    OverviewScreen(
        summary = summary,
        selectedTab = selectedTab,
        onStartSupermarketModeClick = onStartSupermarketMode,
        onSelectTab = { index ->
            selectedTab = index
            onNavigateToTab(index)
        }
    )
}

@Composable
fun OverviewScreen(
    summary: OverviewSummary,
    selectedTab: Int,
    onStartSupermarketModeClick: () -> Unit,
    onSelectTab: (Int) -> Unit
) {
    val bottomItems = listOf(
        BottomNavItem("Início", Icons.Outlined.Home),
        BottomNavItem("Despensa", Icons.Outlined.Inventory2),
        BottomNavItem("Lista", Icons.Outlined.Checklist),
        BottomNavItem("Métricas", Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                bottomItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val itemColor = if (isSelected) ForestGreen else TextSecondary
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onSelectTab(index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = itemColor
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = itemColor,
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = summary.contextLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Visão geral",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 28.sp
                    )
                }
            }

            item {
                StockSummaryCard(summary = summary)
            }

            item {
                NextPurchaseCard(summary = summary)
            }

            item {
                Button(
                    onClick = onStartSupermarketModeClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Iniciar Modo Supermercado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                RestockCard(items = summary.restockItems)
            }
        }
    }
}

@Composable
private fun ListfyCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun StockSummaryCard(summary: OverviewSummary) {
    ListfyCard {
        Column {
            Text(
                text = "Estoque",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricColumn(
                    value = summary.registeredProductsCount.toString(),
                    valueColor = TextPrimary,
                    label = "produtos\ncadastrados",
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    value = summary.criticalOrLowCount.toString(),
                    valueColor = AlertRed,
                    label = "crítico /\nbaixo",
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    value = summary.outOfStockCount.toString(),
                    valueColor = TextPrimary,
                    label = "esgotados",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    valueColor: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun NextPurchaseCard(summary: OverviewSummary) {
    ListfyCard {
        Column {
            Text(
                text = "Próxima compra (estimativa)",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary.formattedCurrency(),
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${summary.totalListItems} itens na lista · ${summary.automaticItemsCount} automáticos, ${summary.manualItemsCount} avulsos",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RestockCard(items: List<RestockItem>) {
    ListfyCard {
        Column {
            Text(
                text = "Precisam de reposição",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                RestockRow(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = DividerSoft,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RestockRow(item: RestockItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
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
        StatusBadge(status = item.status)
    }
}

@Composable
private fun StatusBadge(status: StockStatus) {
    Surface(
        shape = CircleShape,
        color = BadgeBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (status) {
                StockStatus.OUT_OF_STOCK -> {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .border(2.dp, TextSecondary, CircleShape)
                    )
                }
                StockStatus.LOW -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AlertRed)
                    )
                }
            }
            Text(
                text = status.label,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OverviewScreenPreview() {
    ListfyTheme {
        OverviewScreen(
            summary = Overview.initialSummary(),
            selectedTab = 0,
            onStartSupermarketModeClick = {},
            onSelectTab = {}
        )
    }
}