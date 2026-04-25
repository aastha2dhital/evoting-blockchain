package com.example.evotingmobileapp.ui.theme

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
    primary = Color(0xFFD8CCFF),
    onPrimary = Color(0xFF2D1B6B),
    primaryContainer = Color(0xFF4B3795),
    onPrimaryContainer = Color(0xFFF0E9FF),
    inversePrimary = LavenderPrimary,

    secondary = Color(0xFFDCD2FF),
    onSecondary = Color(0xFF2E254D),
    secondaryContainer = Color(0xFF4C416F),
    onSecondaryContainer = Color(0xFFF0EAFF),

    tertiary = Color(0xFFF4C8F6),
    onTertiary = Color(0xFF4A164E),
    tertiaryContainer = Color(0xFF67336D),
    onTertiaryContainer = Color(0xFFFFE9FD),

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF601410),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF9B91AA),
    outlineVariant = Color(0xFF494154),

    scrim = Color.Black,
    inverseSurface = Color(0xFFF1ECF7),
    inverseOnSurface = Color(0xFF252030),
    surfaceTint = Color(0xFFD8CCFF)
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LavenderPrimaryLight,
    onPrimaryContainer = Color(0xFF2D1B6B),
    inversePrimary = Color(0xFFD8CCFF),

    secondary = LavenderSecondary,
    onSecondary = Color.White,
    secondaryContainer = LavenderSecondaryLight,
    onSecondaryContainer = Color(0xFF30264E),

    tertiary = LavenderTertiary,
    onTertiary = Color.White,
    tertiaryContainer = LavenderTertiaryLight,
    onTertiaryContainer = Color(0xFF4A164E),

    background = SoftBackground,
    onBackground = TextPrimaryLight,

    surface = SoftSurface,
    onSurface = TextPrimaryLight,

    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF8A8198),
    outlineVariant = Color(0xFFD7D0E2),

    scrim = Color.Black,
    inverseSurface = Color(0xFF302B3B),
    inverseOnSurface = Color(0xFFF8F3FF),
    surfaceTint = LavenderPrimary
)

@Composable
fun EVotingMobileAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}