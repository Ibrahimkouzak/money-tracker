package com.ibi.moneytracker.ui.theme


import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.sp

// Modern Dashboard Color Scheme
private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2), // Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF009688), // Teal
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),
    background = Color(0xFFF5F6FA), // Light dashboard background
    onBackground = Color(0xFF1E1E2F),
    surface = Color.White,
    onSurface = Color(0xFF1E1E2F),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFB0BEC5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9), // Light Blue
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF80CBC4), // Teal
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFF80CBC4),
    background = Color(0xFF1E1E2F), // Dark dashboard background
    onBackground = Color(0xFFF5F6FA),
    surface = Color(0xFF2C2C3A),
    onSurface = Color(0xFFF5F6FA),
    surfaceVariant = Color(0xFF3A3A4A),
    onSurfaceVariant = Color(0xFFB0B0C0),
    outline = Color(0xFF757575)
)

// Modern Typography for Dashboard
private val DashboardTypography = Typography(
    titleLarge = Typography().titleLarge.copy(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontSize = 16.sp
    ),
    labelLarge = Typography().labelLarge.copy(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )
)

// Rounded shapes for cards and panels
private val DashboardShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16)
)

@Composable
fun MoneyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DashboardTypography,
        shapes = DashboardShapes,
        content = content
    )
}
