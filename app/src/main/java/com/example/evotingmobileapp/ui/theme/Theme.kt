package com.example.evotingmobileapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CivicBlue,
    onPrimary = Color.White,
    primaryContainer = CivicBlueLight,
    onPrimaryContainer = CivicBlueDark,
    inversePrimary = Color(0xFFB9CCFF),

    secondary = ElectionRed,
    onSecondary = Color.White,
    secondaryContainer = ElectionRedLight,
    onSecondaryContainer = ElectionRedDark,

    tertiary = BallotGold,
    onTertiary = Color(0xFF241A00),
    tertiaryContainer = BallotGoldLight,
    onTertiaryContainer = BallotGoldDark,

    background = SnowBackground,
    onBackground = TextPrimaryLight,

    surface = CardSurface,
    onSurface = TextPrimaryLight,

    surfaceVariant = FrostSurface,
    onSurfaceVariant = TextSecondaryLight,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,

    outline = BorderSoft,
    outlineVariant = Color(0xFFE3EAF6),

    scrim = Color.Black,
    inverseSurface = Ink900,
    inverseOnSurface = Color.White,
    surfaceTint = CivicBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8C9FF),
    onPrimary = Color(0xFF06215C),
    primaryContainer = CivicBlueDark,
    onPrimaryContainer = CivicBlueLight,
    inversePrimary = CivicBlue,

    secondary = Color(0xFFFFB3BF),
    onSecondary = Color(0xFF5C0618),
    secondaryContainer = ElectionRedDark,
    onSecondaryContainer = ElectionRedLight,

    tertiary = Color(0xFFFFD78A),
    onTertiary = Color(0xFF4A3000),
    tertiaryContainer = BallotGoldDark,
    onTertiaryContainer = BallotGoldLight,

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorRedLight,

    outline = DarkBorder,
    outlineVariant = Color(0xFF2C354E),

    scrim = Color.Black,
    inverseSurface = Color(0xFFE7ECF6),
    inverseOnSurface = Ink900,
    surfaceTint = Color(0xFFB8C9FF)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
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

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}