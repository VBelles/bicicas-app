package com.tcn.bicicas.ui.theme

import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Blue80,
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Blue40,


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
val LocalDarkTheme = compositionLocalOf { false }


val IncidentColor = Color(0xFFFFA000)
val AvailableColor = Color(0xFF66BB6A)
val ElectricColor = Color(0xFF42A5F5)
val MissingColor = Color.LightGray

val HighAvailabilityColor = Color(0xFF66BB6A)
val LowAvailabilityColor = Color(0xFFFF9800)
val NoAvailabilityColor = Color(0xFFF44336)


val BarTonalElevation = 2.dp
val BarHeight = 58.dp

@Composable
fun Theme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    val indication = rememberRipple()
    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalIndication provides indication,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}