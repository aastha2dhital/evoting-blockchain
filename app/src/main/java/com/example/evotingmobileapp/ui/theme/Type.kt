package com.example.evotingmobileapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.evotingmobileapp.R

private val SecureVoteFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.10).sp
    ),

    displayMedium = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.85).sp
    ),

    displaySmall = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 33.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.60).sp
    ),

    headlineLarge = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 31.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.45).sp
    ),

    headlineMedium = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.35).sp
    ),

    headlineSmall = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.22).sp
    ),

    titleLarge = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.12).sp
    ),

    titleMedium = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    titleSmall = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.01.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.01.sp
    ),

    bodySmall = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.01.sp
    ),

    labelLarge = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.12.sp
    ),

    labelMedium = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.10.sp
    ),

    labelSmall = TextStyle(
        fontFamily = SecureVoteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.10.sp
    )
)