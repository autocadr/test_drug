package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NotoSansKrFontFamily = FontFamily(
  Font(R.font.noto_sans_kr)
)

// All text styles are >= 22.sp for effortless readability by seniors (60+).
val Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
      ),
    headlineLarge =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = NotoSansKrFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
      ),
  )
