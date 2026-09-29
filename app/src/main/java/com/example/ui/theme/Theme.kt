package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val QuietLightColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = InkWhite,
    primaryContainer = SurfaceContainerLow,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = InkWhite,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = PetalAccent,
    onTertiary = TextPrimary,
    background = CanvasSurface,
    onBackground = TextPrimary,
    surface = CanvasSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    outline = BorderOutline,
    outlineVariant = SurfaceContainerHighest
)

@Composable
fun QuietCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = QuietLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuietTypography,
        content = content
    )
}
