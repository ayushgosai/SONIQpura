package com.dwarkadhish.dwarkaspay.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.local.SampleLyricsCatalog
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurface
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    track: Track,
    playbackState: PlaybackState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    val lyrics = remember(track.id) {
        SampleLyricsCatalog.getLyricsForTrack(track.id)
    }

    val activeIndex = remember(playbackState.playbackPositionMs, lyrics) {
        val currentMs = playbackState.playbackPositionMs
        val idx = lyrics.lines.indexOfLast { it.timestampMs <= currentMs }
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.lines.indices) {
            // Keep active lyric smoothly centered
            val target = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepElevatedSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OutlineColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 24.dp)
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Live Lyrics",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${track.title} • ${track.artist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftLavender
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lyrics List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                itemsIndexed(lyrics.lines) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isActive -> TextPrimary
                            isPast -> SoftLavender.copy(alpha = 0.55f)
                            else -> TextTertiary.copy(alpha = 0.35f)
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "lyricTextColor"
                    )

                    val fontSize = if (isActive) 24.sp else 19.sp
                    val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSeekTo(line.timestampMs)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = line.text,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            lineHeight = (fontSize.value * 1.35).sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
