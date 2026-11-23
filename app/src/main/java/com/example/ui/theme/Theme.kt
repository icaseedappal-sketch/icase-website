package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = iCaseBlue,
    onPrimary = Color.White,
    primaryContainer = iCaseNavy,
    onPrimaryContainer = Color.White,
    secondary = iCaseRed,
    onSecondary = Color.White,
    secondaryContainer = iCaseRedLight,
    onSecondaryContainer = iCaseRed,
    background = NavyDark,
    surface = SurfaceDark,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = iCaseBlue,
    onPrimary = Color.White,
    primaryContainer = iCaseLightBlue,
    onPrimaryContainer = iCaseNavy,
    secondary = iCaseRed,
    onSecondary = Color.White,
    secondaryContainer = iCaseRedLight,
    onSecondaryContainer = iCaseRed,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private fun scaledTypography(scale: Float): androidx.compose.material3.Typography {
    if (scale == 1f) return Typography
    fun androidx.compose.ui.text.TextStyle.scaleStyle() = copy(
        fontSize = (fontSize.value * scale).let { androidx.compose.ui.unit.TextUnit(it, fontSize.type) },
        lineHeight = (lineHeight.value * scale).let { androidx.compose.ui.unit.TextUnit(it, lineHeight.type) }
    )
    return Typography.copy(
        displayLarge = Typography.displayLarge.scaleStyle(),
        displayMedium = Typography.displayMedium.scaleStyle(),
        headlineLarge = Typography.headlineLarge.scaleStyle(),
        headlineMedium = Typography.headlineMedium.scaleStyle(),
        headlineSmall = Typography.headlineSmall.scaleStyle(),
        titleLarge = Typography.titleLarge.scaleStyle(),
        titleMedium = Typography.titleMedium.scaleStyle(),
        titleSmall = Typography.titleSmall.scaleStyle(),
        bodyLarge = Typography.bodyLarge.scaleStyle(),
        bodyMedium = Typography.bodyMedium.scaleStyle(),
        bodySmall = Typography.bodySmall.scaleStyle(),
        labelLarge = Typography.labelLarge.scaleStyle(),
        labelMedium = Typography.labelMedium.scaleStyle(),
        labelSmall = Typography.labelSmall.scaleStyle()
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent iCASE branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val spec = LocalResponsiveSpec.current

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography(spec.fontScaleFactor),
        content = content
    )
}
