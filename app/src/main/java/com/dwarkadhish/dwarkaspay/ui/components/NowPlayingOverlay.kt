package com.dwarkadhish.dwarkaspay.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletGlow
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletLight
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

/**
 * Dynamic Island-style floating "Now Playing" overlay.
 *
 * Sits at the very top of the screen (just below the status bar / camera cutout).
 * - Collapsed pill: small album art + scrolling title + play/pause.
 * - Expanded (tap): full controls with prev/play/next + seek progress bar.
 * - Tap expanded again → opens full NowPlayingScreen via [onOpenFullPlayer].
 */
@Composable
fun NowPlayingOverlay(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (playbackState.currentTrack == null) return

    var isExpanded by remember { mutableStateOf(false) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 20.dp else 50.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "corner"
    )
    val dynamicShape = RoundedCornerShape(cornerRadius)

    val borderColor by animateColorAsState(
        targetValue = if (playbackState.isPlaying) ElectricViolet.copy(alpha = 0.6f)
                      else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(600),
        label = "border"
    )

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = dynamicShape,
                    spotColor = ElectricViolet.copy(alpha = 0.4f)
                )
                .clip(dynamicShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1C1B2E), Color(0xFF131220))
                    )
                )
                .border(1.dp, borderColor, dynamicShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isExpanded) onOpenFullPlayer() else isExpanded = true
                }
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    (fadeIn(tween(300)) + expandVertically(tween(350))).togetherWith(
                        fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ).using(SizeTransform(clip = false))
                },
                label = "overlay_content"
            ) { expanded ->
                if (expanded) {
                    ExpandedOverlayContent(
                        playbackState = playbackState,
                        onPlayPauseClick = onPlayPauseClick,
                        onNextClick = onNextClick,
                        onPreviousClick = onPreviousClick,
                        onCollapse = { isExpanded = false }
                    )
                } else {
                    CollapsedPillContent(
                        playbackState = playbackState,
                        onPlayPauseClick = onPlayPauseClick
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Collapsed Pill — artwork + title + play/pause
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CollapsedPillContent(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit
) {
    val track = playbackState.currentTrack ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Live indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (playbackState.isPlaying) ElectricViolet else TextTertiary,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Album art thumbnail
        VioraArtwork(
            artworkUri = track.artworkUri,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Song title
        Text(
            text = track.title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Play / Pause micro-button
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlayPauseClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                tint = ElectricVioletLight,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Expanded — full artwork + metadata + progress + prev/play/next controls
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpandedOverlayContent(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onCollapse: () -> Unit
) {
    val track = playbackState.currentTrack ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Artwork + metadata row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            VioraArtwork(
                artworkUri = track.artworkUri,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ElectricViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (playbackState.isPlaying) {
                MusicVisualizerBars(
                    isPlaying = true,
                    barColor = ElectricViolet,
                    maxHeight = 16.dp,
                    barWidth = 2.5.dp,
                    spacing = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Seek progress bar
        LinearProgressIndicator(
            progress = { playbackState.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50)),
            color = ElectricViolet,
            trackColor = ElectricVioletGlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Controls: Prev — Play/Pause — Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricViolet.copy(alpha = 0.5f),
                                ElectricViolet.copy(alpha = 0.15f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(1.5.dp, ElectricViolet.copy(alpha = 0.7f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlayPauseClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                    tint = TextPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap again to open full player",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
