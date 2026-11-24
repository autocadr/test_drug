package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.SeniorTheme

@Composable
fun WaterCupGrid(
  filledCups: Int,
  goalCups: Int = 8,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val columnsPerRow = if (maxWidth >= 520.dp) 8 else 4
    val rows = (0 until goalCups).chunked(columnsPerRow)

    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      rows.forEach { rowIndices ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          rowIndices.forEach { index ->
            val cupNumber = index + 1
            val isFilled = index < filledCups
            AnimatedWaterCup(
              isFilled = isFilled,
              cupNumber = cupNumber,
              modifier = Modifier.testTag("water_cup_$cupNumber"),
            )
          }
        }
      }
    }
  }
}

@Composable
fun AnimatedWaterCup(
  isFilled: Boolean,
  cupNumber: Int,
  modifier: Modifier = Modifier,
) {
  val colors = SeniorTheme.colors
  val fillProgress by
    animateFloatAsState(
      targetValue = if (isFilled) 1f else 0f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      label = "water_cup_fill_$cupNumber",
    )

  val description =
    if (isFilled) {
      stringResource(R.string.cd_water_cup_filled, cupNumber)
    } else {
      stringResource(R.string.cd_water_cup_empty, cupNumber)
    }

  val emptyBg: Color = colors.waterCupEmptyBg
  val emptyStroke: Color = colors.waterCupEmptyStroke
  val activeStroke: Color = colors.waterActionContainer
  val fillBlue: Color = colors.waterFill
  val fillHighlight: Color = colors.waterFillHighlight

  Canvas(
    modifier =
      modifier
        .width(46.dp)
        .height(56.dp)
        .semantics { contentDescription = description }
  ) {
    val w = size.width
    val h = size.height
    val topInset = w * 0.08f
    val bottomInset = w * 0.22f
    val topY = h * 0.08f
    val bottomY = h * 0.92f

    val cupPath =
      Path().apply {
        moveTo(topInset, topY)
        lineTo(w - topInset, topY)
        lineTo(w - bottomInset, bottomY)
        quadraticTo(w * 0.5f, bottomY + h * 0.05f, bottomInset, bottomY)
        close()
      }

    // Empty glass background
    drawPath(path = cupPath, color = emptyBg)

    // Animated water fill clipped inside the cup silhouette
    if (fillProgress > 0.01f) {
      clipPath(cupPath) {
        val maxWaterHeight = (bottomY - topY) * 0.80f
        val currentWaterTop = bottomY - (maxWaterHeight * fillProgress)

        drawRect(
          color = fillBlue,
          topLeft = Offset(0f, currentWaterTop),
          size = androidx.compose.ui.geometry.Size(w, h - currentWaterTop + 8f),
        )

        // Brighter water surface band for clear visual depth
        drawRect(
          color = fillHighlight,
          topLeft = Offset(0f, currentWaterTop),
          size = androidx.compose.ui.geometry.Size(w, h * 0.11f),
        )
      }
    }

    // Crisp cup rim & outline
    drawPath(
      path = cupPath,
      color = if (isFilled) activeStroke else emptyStroke,
      style =
        Stroke(
          width = if (isFilled) 3.5.dp.toPx() else 2.5.dp.toPx(),
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
  }
}
