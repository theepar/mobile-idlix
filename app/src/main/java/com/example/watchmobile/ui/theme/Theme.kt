package com.example.watchmobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IdlixRed,
    secondary = DarkVariant,
    tertiary = QualityBadgeColor,
    background = TrueBlack,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = DarkVariant
)

@Composable
fun WatchMobileTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            val window = if (context is Activity) {
                context.window
            } else {
                var currentContext = context
                while (currentContext is android.content.ContextWrapper && currentContext !is Activity) {
                    currentContext = currentContext.baseContext
                }
                (currentContext as? Activity)?.window
            }
            
            window?.let {
                it.statusBarColor = TrueBlack.toArgb()
                it.navigationBarColor = TrueBlack.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
