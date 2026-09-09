package com.aistudio.romaniandaily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpruceGreenDark,
    onPrimary = CharcoalDark,
    primaryContainer = SpruceSoftDark,
    onPrimaryContainer = SpruceGreenDark,
    secondary = TerracottaDark,
    onSecondary = CharcoalDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TerracottaDark,
    tertiary = GoodGreenDark,
    error = BadRedDark,
    background = CharcoalDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MutedDark,
    outline = LineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SpruceGreenLight,
    onPrimary = PaperLight,
    primaryContainer = SpruceSoftLight,
    onPrimaryContainer = SpruceGreenLight,
    secondary = TerracottaLight,
    onSecondary = PaperLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = TerracottaLight,
    tertiary = GoodGreenLight,
    error = BadRedLight,
    background = ParchmentLight,
    onBackground = InkLight,
    surface = PaperLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MutedLight,
    outline = LineLight
)

@Composable
fun RomanianDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
