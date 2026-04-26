package com.example.evotingmobileapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.evotingmobileapp.R

private val EVotingFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.8).sp
    ),

    displayMedium = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        lineHeight = 43.sp,
        letterSpacing = (-0.7).sp
    ),

    displaySmall = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 31.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.5).sp
    ),

    headlineLarge = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 29.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.35).sp
    ),

    headlineMedium = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),

    headlineSmall = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.15).sp
    ),

    titleLarge = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.1).sp
    ),

    titleMedium = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    titleSmall = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.02.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.02.sp
    ),

    bodySmall = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.02.sp
    ),

    labelLarge = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.08.sp
    ),

    labelMedium = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.sp
    ),

    labelSmall = TextStyle(
        fontFamily = EVotingFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    )
)