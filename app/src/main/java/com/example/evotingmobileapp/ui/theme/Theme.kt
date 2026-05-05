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
    primary = RoyalIndigo,
    onPrimary = Color.White,
    primaryContainer = RoyalIndigoLight,
    onPrimaryContainer = RoyalIndigoDark,
    inversePrimary = Color(0xFFCBBEFF),

    secondary = EmeraldTeal,
    onSecondary = Color.White,
    secondaryContainer = EmeraldTealLight,
    onSecondaryContainer = EmeraldTealDark,

    tertiary = WarmAmber,
    onTertiary = Color(0xFF2B1A00),
    tertiaryContainer = WarmAmberLight,
    onTertiaryContainer = WarmAmberDark,

    background = AppBackground,
    onBackground = TextPrimaryLight,

    // Important: not pure white anymore
    surface = CardSurface,
    onSurface = TextPrimaryLight,

    // Important: more visible than before
    surfaceVariant = LavenderMist,
    onSurfaceVariant = TextSecondaryLight,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,

    outline = BorderSoft,
    outlineVariant = BorderStrong.copy(alpha = 0.42f),

    scrim = Color.Black,
    inverseSurface = Ink900,
    inverseOnSurface = Color.White,
    surfaceTint = ElectricViolet
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCBBEFF),
    onPrimary = Color(0xFF1D0D49),
    primaryContainer = RoyalIndigoDark,
    onPrimaryContainer = RoyalIndigoLight,
    inversePrimary = RoyalIndigo,

    secondary = Color(0xFF78E6D7),
    onSecondary = Color(0xFF003E37),
    secondaryContainer = EmeraldTealDark,
    onSecondaryContainer = EmeraldTealLight,

    tertiary = Color(0xFFFFD58A),
    onTertiary = Color(0xFF4A3000),
    tertiaryContainer = WarmAmberDark,
    onTertiaryContainer = WarmAmberLight,

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
    outlineVariant = Color(0xFF3A3452),

    scrim = Color.Black,
    inverseSurface = Color(0xFFF1ECFF),
    inverseOnSurface = Ink900,
    surfaceTint = Color(0xFFCBBEFF)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(42.dp)
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