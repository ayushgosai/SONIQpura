package com.dwarkadhish.dwarkaspay.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet

@Composable
fun MusicVisualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = ElectricViolet,
    maxHeight: Dp = 16.dp,
    barWidth: Dp = 3.dp,
    spacing: Dp = 2.dp
) {
    val transition = rememberInfiniteTransition(label = "visualizer")

    val bar1Height by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isPlaying) 1f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isPlaying) 0.85f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.95f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, delayMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val bar4Height by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isPlaying) 0.7f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
    ) {
        val bars = listOf(bar1Height, bar2Height, bar3Height, bar4Height)
        bars.forEach { factor ->
            val height = (maxHeight.value * factor).coerceAtLeast(3f).dp
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(barColor)
            )
        }
    }
}
