package com.tfigo.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// TFI Green palette
private val LightColors = lightColorScheme(
    primary = Color(0xFF1A6B20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA3F49C),
    onPrimaryContainer = Color(0xFF002204),
    secondary = Color(0xFF526350),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8D0),
    onSecondaryContainer = Color(0xFF101F10),
    tertiary = Color(0xFF39656B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF2),
    onTertiaryContainer = Color(0xFF001F23),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C19),
    onSurfaceVariant = Color(0xFF434740),
    outline = Color(0xFF73776F),
    outlineVariant = Color(0xFFC3C8BC),
    surfaceVariant = Color(0xFFE5E5DF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF88D784),
    onPrimary = Color(0xFF00390B),
    primaryContainer = Color(0xFF005315),
    onPrimaryContainer = Color(0xFFA3F49C),
    secondary = Color(0xFFB9CCB5),
    onSecondary = Color(0xFF253424),
    secondaryContainer = Color(0xFF3B4B39),
    onSecondaryContainer = Color(0xFFD5E8D0),
    tertiary = Color(0xFFA1CED5),
    onTertiary = Color(0xFF00363C),
    tertiaryContainer = Color(0xFF1F4D53),
    onTertiaryContainer = Color(0xFFBCEBF2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    onSurfaceVariant = Color(0xFFC3C8BC),
    outline = Color(0xFF8D9187),
    outlineVariant = Color(0xFF434740),
    surfaceVariant = Color(0xFF333531),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TFIGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
