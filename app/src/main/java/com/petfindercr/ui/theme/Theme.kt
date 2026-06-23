package com.petfindercr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple700   = Color(0xFF7C4DFF)
val Purple400   = Color(0xFFB388FF)
val Indigo600   = Color(0xFF4F46E5)
val BgLight     = Color(0xFFF8F9FC)
val LostRed     = Color(0xFFDC2626)
val FoundBlue   = Color(0xFF2563EB)
val SuccessGreen = Color(0xFF22C55E)
val TextPrimary  = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val CardBorder   = Color(0xFFE2E8F0)

private val LightColors = lightColorScheme(
    primary             = Purple700,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFF3E8FF),
    onPrimaryContainer  = Indigo600,
    secondary           = Purple400,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFEDE9FE),
    onSecondaryContainer = Indigo600,
    tertiary            = SuccessGreen,
    onTertiary          = Color.White,
    background          = BgLight,
    onBackground        = TextPrimary,
    surface             = Color.White,
    onSurface           = TextPrimary,
    surfaceVariant      = Color(0xFFF1F5F9),
    onSurfaceVariant    = TextSecondary,
    error               = LostRed,
    onError             = Color.White,
    outline             = CardBorder
)

@Composable
fun PetFinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
