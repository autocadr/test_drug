package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SeniorExtraColors(
  val ivorySurfaceElevated: Color,
  val cardBorder: Color,
  val medDoneContainer: Color,
  val medDoneBorder: Color,
  val medDoneBadgeContainer: Color,
  val onMedDoneContainer: Color,
  val morningIconTint: Color,
  val morningIconBg: Color,
  val lunchIconTint: Color,
  val lunchIconBg: Color,
  val dinnerIconTint: Color,
  val dinnerIconBg: Color,
  val waterCardContainer: Color,
  val waterCardBorder: Color,
  val waterActionContainer: Color,
  val onWaterActionContainer: Color,
  val waterFill: Color,
  val waterFillHighlight: Color,
  val waterCupEmptyBg: Color,
  val waterCupEmptyStroke: Color,
  val waterInk: Color,
  val timerCardContainer: Color,
  val timerCardBorder: Color,
  val timerAccent: Color,
  val timerAlertContainer: Color,
  val timerAlertBorder: Color,
  val timerAlertInk: Color,
)

private val DefaultSeniorExtraColors =
  SeniorExtraColors(
    ivorySurfaceElevated = IvorySurfaceVariant,
    cardBorder = IvoryBorder,
    medDoneContainer = MedDoneGreen,
    medDoneBorder = MedDoneGreenBorder,
    medDoneBadgeContainer = MedDoneBadgeBg,
    onMedDoneContainer = OnMedDoneGreen,
    morningIconTint = MorningIconTint,
    morningIconBg = MorningIconBg,
    lunchIconTint = LunchIconTint,
    lunchIconBg = LunchIconBg,
    dinnerIconTint = DinnerIconTint,
    dinnerIconBg = DinnerIconBg,
    waterCardContainer = WaterCardBg,
    waterCardBorder = WaterCardBorder,
    waterActionContainer = WaterPrimaryBlue,
    onWaterActionContainer = OnWaterPrimary,
    waterFill = WaterFillBlue,
    waterFillHighlight = WaterFillHighlight,
    waterCupEmptyBg = WaterCupEmptyBg,
    waterCupEmptyStroke = WaterCupEmptyStroke,
    waterInk = WaterInk,
    timerCardContainer = TimerCardBg,
    timerCardBorder = TimerCardBorder,
    timerAccent = TimerAccent,
    timerAlertContainer = TimerAlertBg,
    timerAlertBorder = TimerAlertBorder,
    timerAlertInk = TimerAlertInk,
  )

val LocalSeniorColors = staticCompositionLocalOf { DefaultSeniorExtraColors }

private val SeniorIvoryColorScheme =
  lightColorScheme(
    primary = MedDoneGreen,
    onPrimary = OnMedDoneGreen,
    secondary = WaterPrimaryBlue,
    onSecondary = OnWaterPrimary,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = IvoryBorder,
  )

object SeniorTheme {
  val colors: SeniorExtraColors
    @Composable get() = LocalSeniorColors.current
}

@Composable
fun SeniorHealthTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalSeniorColors provides DefaultSeniorExtraColors) {
    MaterialTheme(
      colorScheme = SeniorIvoryColorScheme,
      typography = Typography,
      content = content,
    )
  }
}
