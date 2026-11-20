package com.example.prova6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova6.data.AislesMock
import com.example.prova6.model.AisleGroup
import com.example.prova6.model.AisleProductTemplate
import com.example.prova6.model.MarketAisleCard
import com.example.prova6.repository.AislesRepository
import com.example.prova6.ui.theme.AlertRed
import com.example.prova6.ui.theme.BadgeBackground
import com.example.prova6.ui.theme.CardBorderColor
import com.example.prova6.ui.theme.CardSurface
import com.example.prova6.ui.theme.ForestGreen
import com.example.prova6.ui.theme.ListfyTheme
import com.example.prova6.ui.theme.ScreenBackground
import com.example.prova6.ui.theme.TextPrimary
import com.example.prova6.ui.theme.TextSecondary
import java.util.Locale

 enum class AisleFilter(val label: String) {
    ALL("Todos"),
    FOOD("Alimentos e bebidas"),
    NON_FOOD("Não alimentos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AislesRoute(
    repository: AislesRepository = remember { AislesRepository() },
    onBack: () -> Unit = {},
    onAddProductToShoppingList: (aisle: MarketAisleCard, product: AisleProductTemplate) -> Unit = { _, _ -> }
) {
    var aisles by remember(repository) { mutableStateOf(repository.getAisles()) }
    var products by remember(repository) { mutableStateOf(repository.getProducts()) }
    var selectedFilter by remember { mutableStateOf(AisleFilter.ALL) }

    var editingAisle by remember { mutableStateOf<MarketAisleCard?>(null) }
    var isCreateAisleOpen by remember { mutableStateOf(false) }
    var aisleToDelete by remember { mutableStateOf<MarketAisleCard?>(null) }
    var selectedAisleForProducts by remember { mutableStateOf<MarketAisleCard?>(null) }

    val filteredAisles = remember(aisles, selectedFilter) {
        when (selectedFilter) {
            AisleFilter.ALL -> aisles
            AisleFilter.FOOD -> aisles.filter { it.group == AisleGroup.FOOD_AND_DRINKS }
            AisleFilter.NON_FOOD -> aisles.filter { it.group == AisleGroup.NON_FOOD }
        }
    }

    AislesScreen(
        aisles = filteredAisles,
        products = products,
        selectedFilter = selectedFilter,
        onBack = onBack,
        onSelectFilter = { selectedFilter = it },
        onCreateAisleClick = { isCreateAisleOpen = true },
        onEditAisleClick = { editingAisle = it },
        onDeleteAisleClick = { aisleToDelete = it },
        onOpenAisleProducts = { selectedAisleForProducts = it },
        onQuickAddProductToList = { aisle, product ->
            onAddProductToShoppingList(aisle, product)
        }
    )

    if (isCreateAisleOpen) {
        AisleEditorDialog(
            title = "Novo corredor",
            initialName = "",
            initialDescription = "",
            initialGroup = AisleGroup.FOOD_AND_DRINKS,
            onDismiss = { isCreateAisleOpen = false },
            onSave = { name, description, group ->
                val cleanName = name.trim()
                if (cleanName.isNotEmpty()) {
                    val newCard = MarketAisleCard(
                        id = "aisle_${System.currentTimeMillis()}",
                        name = cleanName,
                        description = description.trim().ifBlank { "Itens organizados neste corredor." },
                        group = group
                    )
                    aisles = aisles + newCard
                }
                isCreateAisleOpen = false
            }
        )
    }

    editingAisle?.let { current ->
        AisleEditorDialog(
            title = "Editar corredor",
            initialName = current.name,
            initialDescription = current.description,
            initialGroup = current.group,
            onDismiss = { editingAisle = null },
            onSave = { name, description, group ->
                val cleanName = name.trim()
                if (cleanName.isNotEmpty()) {
                    aisles = aisles.map { item ->
                        if (item.id == current.id) {
                            item.copy(
                                name = cleanName,
                                description = description.trim().ifBlank { item.description },
                                group = group,
                                sectionHeaderPrefix = cleanName.uppercase()
                            )
                        } else {
                            item
                        }
                    }
                }
                editingAisle = null
            }
        )
    }

    aisleToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { aisleToDelete = null },
            containerColor = CardSurface,
            title = {
                Text(
                    text = "Excluir corredor?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja remover \"${target.name}\" e seus produtos rápidos?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        aisles = aisles.filterNot { it.id == target.id }
                        products = products.filterNot { it.aisleId == target.id }
                        if (selectedAisleForProducts?.id == target.id) {
                            selectedAisleForProducts = null
                        }
                        aisleToDelete = null
                    }
                ) {
                    Text("Excluir", color = AlertRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { aisleToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    selectedAisleForProducts?.let { aisle ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val aisleProducts = products.filter { it.aisleId == aisle.id }

        ModalBottomSheet(
            onDismissRequest = { selectedAisleForProducts = null },
            sheetState = sheetState,
            containerColor = CardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AisleProductsSheetContent(
                aisle = aisle,
                products = aisleProducts,
                onCreateProduct = { prodName, qtyLabel, price ->
                    val cleanProd = prodName.trim()
                    if (cleanProd.isNotEmpty()) {
                        products = products + AisleProductTemplate(
                            id = "prod_${System.currentTimeMillis()}",
                            aisleId = aisle.id,
                            name = cleanProd,
                            defaultQuantityLabel = qtyLabel.trim().ifBlank { "1 un" },
                            estimatedPrice = price
                        )
                    }
                },
                onAddToList = { product ->
                    onAddProductToShoppingList(aisle, product)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AislesScreen(
    aisles: List<MarketAisleCard>,
    products: List<AisleProductTemplate>,
    selectedFilter: AisleFilter,
    onBack: () -> Unit,
    onSelectFilter: (AisleFilter) -> Unit,
    onCreateAisleClick: () -> Unit,
    onEditAisleClick: (MarketAisleCard) -> Unit,
    onDeleteAisleClick: (MarketAisleCard) -> Unit,
    onOpenAisleProducts: (MarketAisleCard) -> Unit,
    onQuickAddProductToList: (MarketAisleCard, AisleProductTemplate) -> Unit
) {
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground,
                    titleContentColor = TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = "Corredores",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Button(
                        onClick = onCreateAisleClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Corredor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AisleFilter.entries) { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) ForestGreen else CardSurface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else CardBorderColor
                            ),
                            modifier = Modifier.clickable { onSelectFilter(filter) }
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            items(aisles, key = { it.id }) { aisle ->
                val aisleProducts = products.filter { it.aisleId == aisle.id }
                MarketAisleCardItem(
                    aisle = aisle,
                    products = aisleProducts,
                    onEditClick = { onEditAisleClick(aisle) },
                    onDeleteClick = { onDeleteAisleClick(aisle) },
                    onManageProductsClick = { onOpenAisleProducts(aisle) },
                    onQuickAddClick = { prod -> onQuickAddProductToList(aisle, prod) }
                )
            }
        }
    }
}

@Composable
private fun MarketAisleCardItem(
    aisle: MarketAisleCard,
    products: List<AisleProductTemplate>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onManageProductsClick: () -> Unit,
    onQuickAddClick: (AisleProductTemplate) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BadgeBackground
                    ) {
                        Text(
                            text = aisle.group.title,
                            color = ForestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = aisle.name,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar corredor",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Excluir corredor",
                            tint = AlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = aisle.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (products.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    products.take(2).forEach { prod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${prod.name} · ${prod.defaultQuantityLabel}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { onQuickAddClick(prod) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "+ Na lista",
                                    color = ForestGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onManageProductsClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Produtos do corredor (${products.size}) · Adicionar ou montar lista",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AisleEditorDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    initialGroup: AisleGroup,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, group: AisleGroup) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var group by remember { mutableStateOf(initialGroup) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AisleGroup.entries.forEach { option ->
                        val selected = option == group
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) ForestGreen else ScreenBackground,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { group = option }
                        ) {
                            Text(
                                text = if (option == AisleGroup.FOOD_AND_DRINKS) "Alimentos" else "Não alimentos",
                                color = if (selected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do corredor") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Itens / descrição") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, description, group) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AisleProductsSheetContent(
    aisle: MarketAisleCard,
    products: List<AisleProductTemplate>,
    onCreateProduct: (name: String, quantityLabel: String, price: Double) -> Unit,
    onAddToList: (AisleProductTemplate) -> Unit
) {
    var prodName by remember { mutableStateOf("") }
    var qtyLabel by remember { mutableStateOf("1 un") }
    var priceText by remember { mutableStateOf("") }

    val locale = Locale.forLanguageTag("pt-BR")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = aisle.name,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = aisle.description,
            color = TextSecondary,
            fontSize = 13.sp
        )

        OutlinedTextField(
            value = prodName,
            onValueChange = { prodName = it },
            placeholder = { Text("Novo produto para este corredor") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = CardBorderColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = qtyLabel,
                onValueChange = { qtyLabel = it },
                label = { Text("Medida") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Preço R$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val parsed = priceText.replace(",", ".").trim().toDoubleOrNull() ?: 0.0
                onCreateProduct(prodName, qtyLabel, parsed)
                prodName = ""
                priceText = ""
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar produto no corredor", fontWeight = FontWeight.SemiBold)
        }

        products.forEach { product ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ScreenBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${product.defaultQuantityLabel} · R$ ${String.format(locale, "%.2f", product.estimatedPrice)}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { onAddToList(product) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AislesScreenPreview() {
    ListfyTheme {
        AislesScreen(
            aisles = AislesMock.initialAisles(),
            products = AislesMock.initialProducts(),
            selectedFilter = AisleFilter.ALL,
            onBack = {},
            onSelectFilter = {},
            onCreateAisleClick = {},
            onEditAisleClick = {},
            onDeleteAisleClick = {},
            onOpenAisleProducts = {},
            onQuickAddProductToList = { _, _ -> }
        )
    }
}