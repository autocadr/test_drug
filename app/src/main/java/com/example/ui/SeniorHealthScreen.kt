package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.MealType
import com.example.ui.components.WaterCupGrid
import com.example.ui.theme.SeniorTheme

@Composable
fun SeniorHealthScreen(
  uiState: SeniorHealthUiState,
  hasNotificationPermission: Boolean,
  onRequestNotificationPermission: () -> Unit,
  onToggleMeal: (MealType) -> Unit,
  onDismissCompletedTimer: () -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val seniorColors = SeniorTheme.colors
  val backgroundBrush =
    Brush.verticalGradient(
      colors =
        listOf(
          seniorColors.ivorySurfaceElevated.copy(alpha = 0.45f),
          MaterialTheme.colorScheme.background,
          MaterialTheme.colorScheme.background,
        )
    )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(backgroundBrush)
          .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      when (uiState) {
        SeniorHealthUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              modifier = Modifier.size(56.dp),
              color = MaterialTheme.colorScheme.primary,
              strokeWidth = 5.dp,
            )
          }
        }

        is SeniorHealthUiState.Ready -> {
          SeniorHealthContent(
            state = uiState,
            hasNotificationPermission = hasNotificationPermission,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onToggleMeal = onToggleMeal,
            onDismissCompletedTimer = onDismissCompletedTimer,
            onAddWaterCup = onAddWaterCup,
            onRemoveWaterCup = onRemoveWaterCup,
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
          )
        }
      }
    }
  }
}

@Composable
private fun SeniorHealthContent(
  state: SeniorHealthUiState.Ready,
  hasNotificationPermission: Boolean,
  onRequestNotificationPermission: () -> Unit,
  onToggleMeal: (MealType) -> Unit,
  onDismissCompletedTimer: () -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  Column(
    modifier =
      modifier
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(22.dp),
  ) {
    // 1. Top Today's Date Header
    TodayDateHeader(
      formattedDate = state.formattedDate,
      completedMeds = state.completedMedsCount,
      totalMeds = state.totalMedsCount,
    )

    // 2. Vertical Medication Buttons (아침 약, 점심 약, 저녁 약)
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      val colors = SeniorTheme.colors

      MedicationCheckButton(
        title = stringResource(R.string.med_morning),
        isTaken = state.morningTaken,
        icon = Icons.Rounded.WbTwilight,
        uncheckedIconTint = colors.morningIconTint,
        uncheckedIconBg = colors.morningIconBg,
        onClick = { onToggleMeal(MealType.MORNING) },
        modifier = Modifier.testTag("meal_button_morning"),
      )

      MedicationCheckButton(
        title = stringResource(R.string.med_lunch),
        isTaken = state.lunchTaken,
        icon = Icons.Rounded.WbSunny,
        uncheckedIconTint = colors.lunchIconTint,
        uncheckedIconBg = colors.lunchIconBg,
        onClick = { onToggleMeal(MealType.LUNCH) },
        modifier = Modifier.testTag("meal_button_lunch"),
      )

      MedicationCheckButton(
        title = stringResource(R.string.med_dinner),
        isTaken = state.dinnerTaken,
        icon = Icons.Rounded.NightsStay,
        uncheckedIconTint = colors.dinnerIconTint,
        uncheckedIconBg = colors.dinnerIconBg,
        onClick = { onToggleMeal(MealType.DINNER) },
        modifier = Modifier.testTag("meal_button_dinner"),
      )
    }

    // 3. Next Dose 8-Hour Countdown Card
    NextDoseCountdownCard(
      timerState = state.nextDoseTimer,
      hasNotificationPermission = hasNotificationPermission,
      onRequestNotificationPermission = onRequestNotificationPermission,
      onDismissCompleted = onDismissCompletedTimer,
    )

    // 4. Water Intake Section at Bottom
    WaterIntakeCard(
      waterCups = state.waterCups,
      goalCups = state.waterGoalCups,
      onAddWaterCup = onAddWaterCup,
      onRemoveWaterCup = onRemoveWaterCup,
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun TodayDateHeader(
  formattedDate: String,
  completedMeds: Int,
  totalMeds: Int,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors
  val allDone = completedMeds >= totalMeds

  Surface(
    modifier = modifier.fillMaxWidth().testTag("today_date_header"),
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(2.dp, colors.cardBorder),
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = stringResource(R.string.cd_calendar_icon),
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
        Text(
          text = stringResource(R.string.today_header_label),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 22.sp,
        )
      }

      Text(
        text = formattedDate,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        modifier = Modifier.testTag("today_date_text"),
      )

      Surface(
        shape = RoundedCornerShape(14.dp),
        color =
          if (allDone) {
            colors.medDoneContainer.copy(alpha = 0.14f)
          } else {
            colors.ivorySurfaceElevated
          },
      ) {
        Text(
          text =
            if (allDone) {
              stringResource(R.string.med_all_done)
            } else {
              stringResource(R.string.med_progress_format, totalMeds, completedMeds)
            },
          style = MaterialTheme.typography.titleMedium,
          color =
            if (allDone) {
              colors.medDoneContainer
            } else {
              MaterialTheme.colorScheme.onSurface
            },
          fontSize = 22.sp,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
      }
    }
  }
}

@Composable
private fun MedicationCheckButton(
  title: String,
  isTaken: Boolean,
  icon: ImageVector,
  uncheckedIconTint: Color,
  uncheckedIconBg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors
  val haptic = LocalHapticFeedback.current

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.medDoneContainer else MaterialTheme.colorScheme.surface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "med_container_color",
    )
  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.medDoneBorder else colors.cardBorder,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "med_border_color",
    )
  val contentColor by
    animateColorAsState(
      targetValue = if (isTaken) colors.onMedDoneContainer else MaterialTheme.colorScheme.onSurface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "med_content_color",
    )

  val statusLabel =
    if (isTaken) {
      stringResource(R.string.med_status_taken)
    } else {
      stringResource(R.string.med_status_pending)
    }

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 100.dp)
        .semantics { stateDescription = statusLabel },
    shape = RoundedCornerShape(22.dp),
    color = containerColor,
    contentColor = contentColor,
    border = BorderStroke(2.5.dp, borderColor),
    tonalElevation = if (isTaken) 4.dp else 1.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(
        modifier =
          Modifier.size(58.dp)
            .clip(CircleShape)
            .background(if (isTaken) colors.medDoneBadgeContainer else uncheckedIconBg),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = stringResource(R.string.cd_pill_icon),
          tint = if (isTaken) colors.onMedDoneContainer else uncheckedIconTint,
          modifier = Modifier.size(34.dp),
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
          color = contentColor,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = statusLabel,
          style = MaterialTheme.typography.titleMedium,
          color =
            if (isTaken) {
              colors.onMedDoneContainer.copy(alpha = 0.95f)
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          fontSize = 22.sp,
          fontWeight = if (isTaken) FontWeight.Bold else FontWeight.SemiBold,
        )
      }

      Icon(
        imageVector =
          if (isTaken) Icons.Rounded.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
        contentDescription =
          if (isTaken) {
            stringResource(R.string.cd_check_done)
          } else {
            stringResource(R.string.cd_check_pending)
          },
        tint = if (isTaken) colors.onMedDoneContainer else colors.cardBorder,
        modifier = Modifier.size(44.dp),
      )
    }
  }
}

@Composable
private fun NextDoseCountdownCard(
  timerState: NextDoseTimerUiState,
  hasNotificationPermission: Boolean,
  onRequestNotificationPermission: () -> Unit,
  onDismissCompleted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors
  val isCompleted = timerState is NextDoseTimerUiState.Completed

  val containerColor =
    when (timerState) {
      is NextDoseTimerUiState.Completed -> colors.timerAlertContainer
      is NextDoseTimerUiState.Counting -> colors.timerCardContainer
      NextDoseTimerUiState.Idle -> MaterialTheme.colorScheme.surface
    }
  val borderColor =
    when (timerState) {
      is NextDoseTimerUiState.Completed -> colors.timerAlertBorder
      is NextDoseTimerUiState.Counting -> colors.timerCardBorder
      NextDoseTimerUiState.Idle -> colors.cardBorder
    }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("next_dose_timer_card"),
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(2.dp, borderColor),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector =
            if (isCompleted) Icons.Rounded.NotificationsActive else Icons.Rounded.Alarm,
          contentDescription = stringResource(R.string.cd_timer_icon),
          tint =
            if (isCompleted) {
              colors.timerAlertInk
            } else {
              colors.timerAccent
            },
          modifier = Modifier.size(30.dp),
        )
        Text(
          text = stringResource(R.string.timer_card_title),
          style = MaterialTheme.typography.headlineMedium,
          color =
            if (isCompleted) {
              colors.timerAlertInk
            } else {
              MaterialTheme.colorScheme.onSurface
            },
          fontSize = 25.sp,
        )
      }

      when (timerState) {
        NextDoseTimerUiState.Idle -> {
          Text(
            text = stringResource(R.string.timer_idle_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 22.sp,
          )
        }

        is NextDoseTimerUiState.Counting -> {
          Text(
            text = stringResource(R.string.timer_last_meal_format, timerState.triggerMealLabel),
            style = MaterialTheme.typography.titleMedium,
            color = colors.timerAccent,
            fontSize = 22.sp,
          )
          Text(
            text = timerState.formattedRemaining,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 34.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("next_dose_countdown_text"),
          )
          Text(
            text = stringResource(R.string.timer_scheduled_at_format, timerState.scheduledTimeLabel),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 22.sp,
          )
        }

        is NextDoseTimerUiState.Completed -> {
          Text(
            text = stringResource(R.string.timer_done_title),
            style = MaterialTheme.typography.headlineLarge,
            color = colors.timerAlertInk,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text = stringResource(R.string.timer_done_subtitle),
            style = MaterialTheme.typography.titleLarge,
            color = colors.timerAlertInk,
            fontSize = 23.sp,
          )
          Button(
            onClick = onDismissCompleted,
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 60.dp)
                .testTag("dismiss_timer_button"),
            shape = RoundedCornerShape(16.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = colors.timerAlertBorder,
                contentColor = Color.White,
              ),
          ) {
            Text(
              text = stringResource(R.string.timer_dismiss_button),
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontSize = 22.sp,
            )
          }
        }
      }

      if (!hasNotificationPermission) {
        OutlinedButton(
          onClick = onRequestNotificationPermission,
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 58.dp)
              .testTag("enable_notification_button"),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(2.dp, colors.timerCardBorder),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = colors.timerAccent,
            ),
        ) {
          Text(
            text = stringResource(R.string.timer_enable_notification),
            style = MaterialTheme.typography.titleMedium,
            color = colors.timerAccent,
            fontSize = 22.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun WaterIntakeCard(
  waterCups: Int,
  goalCups: Int,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors
  val haptic = LocalHapticFeedback.current
  val goalReached = waterCups >= goalCups

  Surface(
    modifier = modifier.fillMaxWidth().testTag("water_intake_card"),
    shape = RoundedCornerShape(24.dp),
    color = colors.waterCardContainer,
    border = BorderStroke(2.dp, colors.waterCardBorder),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(R.string.water_section_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.waterInk,
            fontSize = 26.sp,
          )
          Text(
            text =
              if (goalReached) {
                stringResource(R.string.water_goal_reached)
              } else {
                stringResource(R.string.water_goal_format, goalCups)
              },
            style = MaterialTheme.typography.titleMedium,
            color = colors.waterInk.copy(alpha = 0.85f),
            fontSize = 22.sp,
          )
        }

        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(2.dp, colors.waterCardBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = waterCups.toString(),
              style = MaterialTheme.typography.displayLarge,
              color = colors.waterActionContainer,
              fontSize = 40.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.testTag("water_count_number"),
            )
            Text(
              text = stringResource(R.string.water_cup_unit),
              style = MaterialTheme.typography.titleLarge,
              color = colors.waterInk,
              fontSize = 24.sp,
              modifier = Modifier.padding(bottom = 4.dp),
            )
          }
        }
      }

      WaterCupGrid(
        filledCups = waterCups,
        goalCups = goalCups,
        modifier = Modifier.padding(vertical = 4.dp),
      )

      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onAddWaterCup()
        },
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 82.dp)
            .testTag("add_water_button"),
        shape = RoundedCornerShape(20.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = colors.waterActionContainer,
            contentColor = colors.onWaterActionContainer,
          ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.WaterDrop,
          contentDescription = stringResource(R.string.cd_water_drop),
          modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.water_add_button),
          style = MaterialTheme.typography.titleLarge,
          color = colors.onWaterActionContainer,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      AnimatedVisibility(visible = waterCups > 0) {
        OutlinedButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRemoveWaterCup()
          },
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 58.dp)
              .testTag("remove_water_button"),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(2.dp, colors.waterCardBorder),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = colors.waterInk,
            ),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Remove,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.water_remove_button),
            style = MaterialTheme.typography.titleMedium,
            color = colors.waterInk,
            fontSize = 22.sp,
          )
        }
      }
    }
  }
}
