package cn.zhzgo.study.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlack,
    onPrimary = BackgroundWhite,
    secondary = SuccessGreen,
    onSecondary = BackgroundWhite,
    background = PrimaryBlack,
    surface = Color(0xFF1E1E1E),
    onSurface = BackgroundWhite,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlack,
    onPrimary = BackgroundWhite,
    secondary = SuccessGreen,
    onSecondary = BackgroundWhite,
    background = SurfaceGray,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    primaryColorOverride: Color? = null,
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    if (primaryColorOverride != null) {
        colorScheme = colorScheme.copy(
            primary = primaryColorOverride
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
