package com.example.messenger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0088CC),
    secondary = Color(0xFF8E8E93),
    onPrimary = Color.White,
    background = Color(0xFF17212B),
    surface = Color(0xFF242F3D),
    onSurface = Color.White,
    outline = Color(0xFF101921),
    surfaceVariant = Color(0xFF242F3D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0088CC),
    secondary = Color(0xFF8E8E93),
    onPrimary = Color.White,
    background = Color.White,
    surface = Color(0xFFD5D5D5),
    onSurface = Color.Black,
    outline = Color(0xFFD0D2D5),
    surfaceVariant = Color(0xFFEBEDF0)
)

@Composable
fun MessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    multiplier: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(multiplier),
        content = content
    )
}
fun TextStyle.scale(multiplier: Float): TextStyle {
    return this.copy(fontSize = this.fontSize * multiplier)
}

@Composable
fun getTypography(multiplier: Float): Typography {
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.scale(multiplier),
        displayMedium = default.displayMedium.scale(multiplier),
        displaySmall = default.displaySmall.scale(multiplier),
        headlineLarge = default.headlineLarge.scale(multiplier),
        headlineMedium = default.headlineMedium.scale(multiplier),
        headlineSmall = default.headlineSmall.scale(multiplier),
        titleLarge = default.titleLarge.scale(multiplier),
        titleMedium = default.titleMedium.scale(multiplier),
        titleSmall = default.titleSmall.scale(multiplier),
        bodyLarge = default.bodyLarge.scale(multiplier),
        bodyMedium = default.bodyMedium.scale(multiplier),
        bodySmall = default.bodySmall.scale(multiplier),
        labelLarge = default.labelLarge.scale(multiplier),
        labelMedium = default.labelMedium.scale(multiplier),
        labelSmall = default.labelSmall.scale(multiplier)
    )
}