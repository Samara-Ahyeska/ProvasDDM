package com.example.prova6.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ScreenBackground = Color(0xFFF6F5F0)
val CardSurface = Color(0xFFFCFBF7)
val CardBorderColor = Color(0xFFECEAE2)
val ForestGreen = Color(0xFF1F6343)
val AlertRed = Color(0xFFC62828)
val BadgeBackground = Color(0xFFF2EFE9)
val TextPrimary = Color(0xFF1C1F1D)
val TextSecondary = Color(0xFF6E736F)
val DividerSoft = Color(0xFFF0EEE8)

private val ListfyLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    background = ScreenBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = AlertRed,
    outline = CardBorderColor
)

@Composable
fun ListfyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ListfyLightColorScheme,
        content = content
    )
}