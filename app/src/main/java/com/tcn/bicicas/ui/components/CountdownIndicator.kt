package com.tcn.bicicas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun CountDownIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    stroke: Dp,
) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    Box(modifier = modifier) {
        val color = MaterialTheme.colorScheme.primary
        val strokePixels = with(LocalDensity.current) { stroke.toPx() }
        Canvas(
            Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .align(Alignment.Center)
        ) {
            drawCircularIndicator(1f, color.copy(alpha = 0.1f), Stroke(strokePixels))
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .align(Alignment.Center)
        ) {
            drawCircularIndicator(
                animatedProgress,
                color,
                Stroke(strokePixels, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawCircularIndicator(
    progress: Float,
    color: Color,
    stroke: Stroke
) {
    // Start at 12 O'clock
    val startAngle = 270f
    val sweep = progress * 360f

    // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
    // To do this we need to remove half the stroke width from the total diameter for both sides.
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke
    )
}