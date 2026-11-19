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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prova5.data.model.BudgetCycleConfig
import com.example.prova5.data.model.CycleResetMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmeraldDark = Color(0xFF136F48)
private val EmeraldSoftBg = Color(0xFFE3F2EB)
private val ScreenBg = Color(0xFFF6F7F8)
private val TextDark = Color(0xFF1A1C1E)
private val TextMuted = Color(0xFF6C757D)
private val CardBorder = Color(0xFFE7EAEF)
private val StatusRed = Color(0xFFD93025)

@Composable
fun EditProfileScreen(
    currentName: String,
    currentNickname: String,
    userEmail: String,
    cycleConfig: BudgetCycleConfig = BudgetCycleConfig(),
    cycleStartMillis: Long = System.currentTimeMillis(),
    onCycleModeChange: (CycleResetMode) -> Unit = {},
    onAnchorDateChange: (Long) -> Unit = {},
    onResetCycleNow: () -> Unit = {},
    onBack: () -> Unit,
    onSaveProfile: (
        newName: String,
        newNickname: String,
        currentPassword: String,
        newPassword: String?,
        onError: (String) -> Unit
    ) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    var nameInput by remember { mutableStateOf(currentName) }
    var nicknameInput by remember { mutableStateOf(currentNickname) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedMode by remember(cycleConfig.mode) { mutableStateOf(cycleConfig.mode) }
    var selectedAnchorMillis by remember(cycleConfig.anchorDateMillis) { mutableStateOf(cycleConfig.anchorDateMillis) }
    var showResetCycleDialog by remember { mutableStateOf(false) }
    var cycleFeedback by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    }
    val formattedCycleStart = remember(cycleStartMillis, selectedMode, selectedAnchorMillis) {
        dateFormatter.format(Date(cycleStartMillis))
    }

    val previewInitial = remember(nicknameInput, nameInput, userEmail) {
        val base = nicknameInput.ifBlank { nameInput.ifBlank { userEmail } }.trim()
        base.firstOrNull()?.uppercase() ?: "S"
    }

    val previewGreeting = remember(nicknameInput, nameInput) {
        val target = nicknameInput.trim().ifBlank { nameInput.trim().substringBefore(" ") }
        if (target.isNotBlank()) "Olá, $target!" else "Olá!"
    }

    fun openCalendarPicker(
        initialMillis: Long,
        onDateSelected: (millis: Long) -> Unit
    ) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(chosen.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showResetCycleDialog) {
        AlertDialog(
            onDismissRequest = { showResetCycleDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Iniciar novo ciclo hoje?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    text = "Os gastos exibidos nos tetos deste ciclo passarão a contar a partir de hoje. Os valores dos limites e o histórico completo continuarão salvos.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetCycleDialog = false
                        val now = System.currentTimeMillis()
                        selectedAnchorMillis = now
                        onResetCycleNow()
                        cycleFeedback = "Novo ciclo iniciado hoje. Tetos zerados para o novo período."
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldDark,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirmar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetCycleDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
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
                text = "Meu perfil",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Personalize como o app chama você, ajuste o ciclo dos tetos e gerencie seus dados de acesso.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(EmeraldSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = previewInitial,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = previewGreeting,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prévia da saudação na tela inicial",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "CICLO DE RENOVAÇÃO DOS TETOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldSoftBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Renovação automática",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Ciclo atual desde $formattedCycleStart",
                                fontSize = 12.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileCycleModeChip(
                            title = "A cada 30 dias",
                            subtitle = "Ciclo mensal",
                            selected = selectedMode == CycleResetMode.EVERY_30_DAYS,
                            onClick = {
                                selectedMode = CycleResetMode.EVERY_30_DAYS
                                cycleFeedback = null
                                onCycleModeChange(CycleResetMode.EVERY_30_DAYS)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileCycleModeChip(
                            title = "A cada 15 dias",
                            subtitle = "Ideal para vale",
                            selected = selectedMode == CycleResetMode.EVERY_15_DAYS,
                            onClick = {
                                selectedMode = CycleResetMode.EVERY_15_DAYS
                                cycleFeedback = null
                                onCycleModeChange(CycleResetMode.EVERY_15_DAYS)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val cycleDaysLabel = if (selectedMode == CycleResetMode.EVERY_30_DAYS) "30 dias" else "15 dias"
                    val anchorFormatted = remember(selectedAnchorMillis) {
                        dateFormatter.format(Date(selectedAnchorMillis))
                    }

                    Text(
                        text = "Data base de início do ciclo de $cycleDaysLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        onClick = {
                            openCalendarPicker(selectedAnchorMillis) { chosenMillis ->
                                selectedAnchorMillis = chosenMillis
                                cycleFeedback = null
                                onAnchorDateChange(chosenMillis)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = ScreenBg,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (selectedMode == CycleResetMode.EVERY_30_DAYS) {
                                        Icons.Outlined.CalendarMonth
                                    } else {
                                        Icons.Outlined.Schedule
                                    },
                                    contentDescription = "Selecionar data no calendário",
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "A cada $cycleDaysLabel desde $anchorFormatted",
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                            }
                            Text(
                                text = "Calendário",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldDark
                            )
                        }
                    }

                    if (cycleFeedback != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = cycleFeedback!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mantém limites e histórico",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { showResetCycleDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = EmeraldDark)
                        ) {
                            Text(
                                text = "Iniciar novo ciclo hoje",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NOME COMPLETO *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMessage = null
                },
                placeholder = { Text("Digite seu nome", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "APELIDO (COMO QUER SER CHAMADO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nicknameInput,
                onValueChange = {
                    nicknameInput = it
                    errorMessage = null
                },
                placeholder = { Text("Ex: Samy, Ana, Leo...", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "E-MAIL (NÃO EDITÁVEL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color(0xFFECEEF1),
                    disabledBorderColor = CardBorder,
                    disabledTextColor = TextMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SENHA ATUAL (PARA ALTERAR SENHA)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentPasswordInput,
                onValueChange = {
                    currentPasswordInput = it
                    errorMessage = null
                },
                placeholder = { Text("Necessária ao definir uma nova senha", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                        Icon(
                            imageVector = if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (currentPasswordVisible) "Ocultar senha atual" else "Mostrar senha atual",
                            tint = TextMuted
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EmeraldDark,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "NOVA SENHA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPasswordInput,
                onValueChange = {
                    newPasswordInput = it
                    errorMessage = null
                },
                placeholder = { Text("Deixe em branco para manter a atual", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (newPasswordVisible) "Ocultar nova senha" else "Mostrar nova senha",
                            tint = TextMuted
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EmeraldDark,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

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
                    val trimmedName = nameInput.trim()
                    val trimmedNickname = nicknameInput.trim()
                    val currentPass = currentPasswordInput
                    val newPass = newPasswordInput

                    if (trimmedName.isBlank()) {
                        errorMessage = "O nome não pode ficar em branco."
                    } else if (newPass.isNotEmpty()) {
                        if (currentPass.isBlank()) {
                            errorMessage = "Informe sua senha atual para confirmar a troca."
                        } else if (newPass.length < 6) {
                            errorMessage = "A nova senha deve ter pelo menos 6 caracteres."
                        } else {
                            onSaveProfile(trimmedName, trimmedNickname, currentPass, newPass) { msg ->
                                errorMessage = msg
                            }
                        }
                    } else {
                        onSaveProfile(trimmedName, trimmedNickname, currentPass, null) { msg ->
                            errorMessage = msg
                        }
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
                    text = "Salvar alterações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogout,
                shape = CircleShape,
                border = BorderStroke(1.dp, StatusRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Sair da conta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "Cancelar",
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
private fun ProfileCycleModeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) EmeraldSoftBg else ScreenBg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) EmeraldDark else CardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) EmeraldDark else TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}