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
    primary = VotingSky,
    onPrimary = Color.White,
    primaryContainer = VotingBlue,
    onPrimaryContainer = TextPrimaryDark,

    secondary = TrustTeal,
    onSecondary = Color.White,
    secondaryContainer = TrustTealDark,
    onSecondaryContainer = TextPrimaryDark,

    tertiary = AccentGold,
    onTertiary = Color.Black,
    tertiaryContainer = AccentGoldDark,
    onTertiaryContainer = Color.White,

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VotingBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = VotingBlueDark,

    secondary = TrustTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F3EF),
    onSecondaryContainer = TrustTealDark,

    tertiary = AccentGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFEDC2),
    onTertiaryContainer = AccentGoldDark,

    background = SoftBackground,
    onBackground = TextPrimaryLight,

    surface = SoftSurface,
    onSurface = TextPrimaryLight,

    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,

    error = ErrorRed,
    onError = Color.White
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