package cn.zhzgo.study.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    secondary = PureWhite,
    onSecondary = PureBlack,
    tertiary = PureWhite,
    onTertiary = PureBlack,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkOutline,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    secondary = PureBlack,
    onSecondary = PureWhite,
    tertiary = PureBlack,
    onTertiary = PureWhite,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightOutline,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = ErrorColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We explicitly disable dynamicColor defaults everywhere to uphold the Black/White strict aesthetic.
    dynamicColor: Boolean = false,
    primaryColorOverride: Color? = null,
    content: @Composable () -> Unit
) {
    var colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    if (primaryColorOverride != null) {
        colorScheme = colorScheme.copy(
            primary = primaryColorOverride
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Remove the hardcoded status bar coloring so EdgeToEdge handles it natively.
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
