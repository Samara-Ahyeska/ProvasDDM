package com.example.prova5.ui.dashboard

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova5.data.model.ExpenseCategory
import com.example.prova5.data.model.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmeraldDark = Color(0xFF136F48)
private val ScreenBg = Color(0xFFF6F7F8)
private val TextDark = Color(0xFF1A1C1E)
private val TextMuted = Color(0xFF6C757D)
private val CardBorder = Color(0xFFE7EAEF)
private val StatusRed = Color(0xFFD93025)

@Composable
fun AddExpenseScreen(
    availableCategories: List<ExpenseCategory>,
    onBack: () -> Unit,
    onSave: (Double, ExpenseCategory, String, PaymentMethod, String, Long) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory>(availableCategories.firstOrNull() ?: ExpenseCategory.SUPERMERCADO) }
    var description by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf<PaymentMethod>(PaymentMethod.PIX) }

    var creditType by remember { mutableStateOf("À vista") }
    var installments by remember { mutableIntStateOf(2) }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val formattedDateText = remember(selectedTimestamp) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
        val todayCal = Calendar.getInstance()
        val isToday = cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            "Hoje, " + SimpleDateFormat("d 'nov' yyyy", Locale("pt", "BR")).format(Date(selectedTimestamp))
        } else {
            SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(Date(selectedTimestamp))
                .replaceFirstChar { it.uppercase() }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = EmeraldDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Voltar",
                    color = EmeraldDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nova compra",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VALOR DA COMPRA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawAmount,
                        onValueChange = {
                            rawAmount = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                "R$ 0,00",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Somente valores numéricos maiores que zero",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CATEGORIA *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            availableCategories.chunked(2).forEach { rowCats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCats.forEach { cat ->
                        val isSelected = selectedCategory.name == cat.name
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) EmeraldDark else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) EmeraldDark else CardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = cat.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.title.substringBefore(" /"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else TextDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (rowCats.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "DESCRIÇÃO / ESTABELECIMENTO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Ex: Farmácia São João, Posto Petrobras", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EmeraldDark,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FORMA DE PAGAMENTO *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val paymentMethods = PaymentMethod.defaultList
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedPayment.name == method.name
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) EmeraldDark else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldDark else CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable { selectedPayment = method }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = method.label.replace("Cartão de ", ""),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextDark
                            )
                        }
                    }
                }
            }

            if (selectedPayment.name == PaymentMethod.CREDITO.name) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TIPO DE CRÉDITO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("À vista", "Parcelado").forEach { type ->
                                val isSubSelected = creditType == type
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSubSelected) EmeraldDark else ScreenBg,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clickable { creditType = type }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = type,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSubSelected) Color.White else TextDark
                                        )
                                    }
                                }
                            }
                        }

                        if (creditType == "Parcelado") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Número de parcelas: ${installments}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (2..6).forEach { num ->
                                    val isChosen = installments == num
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isChosen) EmeraldDark else ScreenBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clickable { installments = num }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${num}x",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) Color.White else TextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DATA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth)
                                }
                                selectedTimestamp = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDateText,
                        fontSize = 15.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Alterar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
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
                    val cleanVal = rawAmount.replace("R$", "").replace(".", "").replace(",", ".").trim()
                    val amount = cleanVal.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Digite um valor numérico válido maior que zero."
                    } else {
                        val details = if (selectedPayment.name == PaymentMethod.CREDITO.name) {
                            if (creditType == "À vista") "Crédito à vista" else "Crédito parcelado em ${installments}x"
                        } else {
                            ""
                        }
                        onSave(amount, selectedCategory, description, selectedPayment, details, selectedTimestamp)
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
                    text = "Salvar compra",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}