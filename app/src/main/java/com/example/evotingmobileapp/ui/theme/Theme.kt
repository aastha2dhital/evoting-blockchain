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
    inversePrimary = AccentGold,

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
    onError = Color.White,
    errorContainer = Color(0xFF601410),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF6F7D90),
    outlineVariant = Color(0xFF314158),

    scrim = Color.Black,
    inverseSurface = Color(0xFFE7EDF7),
    inverseOnSurface = Color(0xFF152033),
    surfaceTint = VotingSky
)

private val LightColorScheme = lightColorScheme(
    primary = VotingBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E7FF),
    onPrimaryContainer = VotingBlueDark,
    inversePrimary = VotingSky,

    secondary = TrustTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F4F0),
    onSecondaryContainer = TrustTealDark,

    tertiary = AccentGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFEDC7),
    onTertiaryContainer = AccentGoldDark,

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

    outline = Color(0xFF758399),
    outlineVariant = Color(0xFFC9D3E1),

    scrim = Color.Black,
    inverseSurface = Color(0xFF1B2430),
    inverseOnSurface = Color(0xFFF4F7FB),
    surfaceTint = VotingBlue
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