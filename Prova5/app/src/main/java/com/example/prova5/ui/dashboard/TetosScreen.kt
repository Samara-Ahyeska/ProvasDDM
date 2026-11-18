package com.example.prova5.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova5.data.model.ExpenseCategory

private val EmeraldDark = Color(0xFF136F48)
private val ScreenBg = Color(0xFFF6F7F8)
private val TextDark = Color(0xFF1A1C1E)
private val TextMuted = Color(0xFF6C757D)
private val CardBorder = Color(0xFFE7EAEF)
private val StatusRed = Color(0xFFD93025)
private val StatusRedBg = Color(0xFFFCE8E6)

@Composable
fun TetosScreen(
    currentBudgets: Map<ExpenseCategory, Double>,
    onSaveBudget: (ExpenseCategory, Double) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var supermercadoInput by rememberSaveable { mutableStateOf(currentBudgets[ExpenseCategory.SUPERMERCADO]?.toString() ?: "1200.00") }
    var combustivelInput by rememberSaveable { mutableStateOf(currentBudgets[ExpenseCategory.COMBUSTIVEL]?.toString() ?: "500.00") }
    var farmaciaInput by rememberSaveable { mutableStateOf(currentBudgets[ExpenseCategory.FARMACIA]?.toString() ?: "-250,00") }


    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "PASSO FINAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Defina seus tetos mensais",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Quanto você pretende gastar por mês em cada categoria? Você pode alterar depois em Tetos.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            BudgetInputCard(
                icon = "🛒",
                title = "Supermercado",
                subtitle = "Ex.: feira e mercado do mês",
                value = supermercadoInput,
                onValueChange = { supermercadoInput = it },
                isSelected = true,
                isError = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            BudgetInputCard(
                icon = "⛽",
                title = "Combustível",
                subtitle = "Ex.: gasolina e transporte",
                value = combustivelInput,
                onValueChange = { combustivelInput = it },
                isSelected = false,
                isError = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            BudgetInputCard(
                icon = "💊",
                title = "Farmácia",
                subtitle = "Informe um valor maior que zero",
                value = farmaciaInput,
                onValueChange = { farmaciaInput = it },
                isSelected = false,
                isError = true,
                errorText = "Informe um valor maior que zero"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, EmeraldDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { }
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Apenas números positivos. Campos com letras ou valores negativos não são aceitos.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val supVal = supermercadoInput.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()
                    val combVal = combustivelInput.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()
                    val farmVal = farmaciaInput.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()

                    if (supVal == null || supVal <= 0.0 || combVal == null || combVal <= 0.0 || farmVal == null || farmVal <= 0.0) {
                        errorMessage = "Por favor, corrija os valores inválidos ou negativos antes de continuar."
                    } else {
                        errorMessage = null
                        onSaveBudget(ExpenseCategory.SUPERMERCADO, supVal)
                        onSaveBudget(ExpenseCategory.COMBUSTIVEL, combVal)
                        onSaveBudget(ExpenseCategory.FARMACIA, farmVal)
                        onContinue()
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Salvar e continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Pular por agora",
                        color = EmeraldDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetInputCard(
    icon: String,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    isSelected: Boolean,
    isError: Boolean,
    errorText: String? = null
) {
    val borderColor = if (isError) StatusRed else if (isSelected) Color(0xFF1A73E8) else CardBorder
    val borderWidth = if (isSelected || isError) 2.dp else 1.dp

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isError) StatusRedBg.copy(alpha = 0.4f) else Color.White,
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF7F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isError && errorText != null) errorText else subtitle,
                    fontSize = 12.sp,
                    color = if (isError) StatusRed else TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, if (isError) StatusRed else CardBorder),
                modifier = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) StatusRed else TextDark
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}