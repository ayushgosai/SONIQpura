package com.dwarkadhish.dwarkaspay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dwarkadhish.dwarkaspay.model.RepeatMode
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletGradient
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    modifier: Modifier = Modifier,
    playButtonSize: Dp = 68.dp,
    iconButtonSize: Dp = 48.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Toggle
        IconButton(
            onClick = onShuffleClick,
            modifier = Modifier.size(iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffleEnabled) ElectricViolet else TextTertiary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Previous
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier.size(iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous Track",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        // Big Play/Pause Button
        Box(
            modifier = Modifier
                .size(playButtonSize)
                .shadow(12.dp, CircleShape, spotColor = ElectricViolet)
                .clip(CircleShape)
                .background(ElectricVioletGradient)
                .clickable(onClick = onPlayPauseClick),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Next
        IconButton(
            onClick = onNextClick,
            modifier = Modifier.size(iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next Track",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        // Repeat Toggle
        IconButton(
            onClick = onRepeatClick,
            modifier = Modifier.size(iconButtonSize)
        ) {
            val repeatIcon = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
            val isRepeatActive = repeatMode != RepeatMode.OFF
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat",
                tint = if (isRepeatActive) ElectricViolet else TextTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
