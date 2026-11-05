package com.dwarkadhish.dwarkaspay.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.local.SampleLyricsCatalog
import com.dwarkadhish.dwarkaspay.model.EqualizerSettings
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.SleepTimerOption
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.PlaybackControls
import com.dwarkadhish.dwarkaspay.ui.components.VioraArtwork
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurface
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.PlayerBackgroundGradient
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playbackState: PlaybackState,
    equalizerSettings: EqualizerSettings = EqualizerSettings(),
    sleepTimerOption: SleepTimerOption = SleepTimerOption.OFF,
    sleepTimerRemainingSeconds: Long? = null,
    onCollapse: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekToMs: (Long) -> Unit = {},
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onOpenQueue: () -> Unit,
    onUpdateEqualizer: (EqualizerSettings) -> Unit = {},
    onSelectSleepTimer: (SleepTimerOption) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack ?: return

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isDraggingSlider) dragProgress else playbackState.progress

    val heartScale by animateFloatAsState(
        targetValue = if (track.isFavorite) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heartScale"
    )

    // Sheets visibility
    var showTrackOptions by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lyricsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val equalizerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sleepTimerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun formatMs(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format(Locale.getDefault(), "%d:%02d", m, s)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerBackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Collapse Arrow, Playlist Context, Timer Pill, More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM PLAYLIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ElectricViolet
                    )
                    Text(
                        text = track.album.ifBlank { "SONIQ Midnight" },
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricViolet.copy(alpha = 0.2f))
                                .clickable { showSleepTimerSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🌙 ${formatMs(sleepTimerRemainingSeconds * 1000)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = { showTrackOptions = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Immersive Album Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 12.dp)
                    .shadow(32.dp, RoundedCornerShape(24.dp), spotColor = ElectricViolet)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                VioraArtwork(
                    artworkUri = track.artworkUri,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 16.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Title, Artist, and Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${track.artist} • ${track.genre}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onFavoriteToggle(track) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) ElectricViolet else TextTertiary,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(heartScale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seek Bar Slider & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentProgress,
                    onValueChange = {
                        isDraggingSlider = true
                        dragProgress = it
                    },
                    onValueChangeFinished = {
                        onSeek(dragProgress)
                        isDraggingSlider = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricViolet,
                        activeTrackColor = ElectricViolet,
                        inactiveTrackColor = OutlineColor
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentMs = if (isDraggingSlider) {
                        (dragProgress * playbackState.durationMs).toLong()
                    } else {
                        playbackState.playbackPositionMs
                    }
                    val remainingMs = (playbackState.durationMs - currentMs).coerceAtLeast(0L)

                    Text(
                        text = formatMs(currentMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "-${formatMs(remainingMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Controls Row
            PlaybackControls(
                isPlaying = playbackState.isPlaying,
                isLoading = playbackState.isLoading,
                isShuffleEnabled = playbackState.isShuffleEnabled,
                repeatMode = playbackState.repeatMode,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPreviousClick = onPreviousClick,
                onShuffleClick = onShuffleToggle,
                onRepeatClick = onRepeatToggle
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Synchronized Lyrics Preview Card (Spotify-Style)
            val currentLyric = remember(playbackState.playbackPositionMs, track.id) {
                val lyrics = SampleLyricsCatalog.getLyricsForTrack(track.id)
                lyrics.lines.lastOrNull { it.timestampMs <= playbackState.playbackPositionMs }?.text
                    ?: "Tap to view live lyrics"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardSurfaceVariant.copy(alpha = 0.8f))
                    .clickable { showLyricsSheet = true }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = null,
                                tint = ElectricViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LYRICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ElectricViolet
                            )
                        }

                        Text(
                            text = "EXPAND",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SoftLavender
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentLyric,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Actions Bar: Audio Quality, Equalizer FX, Sleep Timer, Queue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEqualizerSheet = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Audio Equalizer",
                        tint = if (equalizerSettings.isEnabled) ElectricViolet else SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (equalizerSettings.isEnabled) "Hi-Fi • ${equalizerSettings.presetName}" else "Hi-Fi 320k",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (equalizerSettings.isEnabled) ElectricViolet else SoftLavender
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showSleepTimerSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerOption != SleepTimerOption.OFF) ElectricViolet else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onOpenQueue) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Track Options Bottom Sheet
        if (showTrackOptions) {
            TrackOptionsBottomSheet(
                track = track,
                sheetState = optionsSheetState,
                onDismiss = { showTrackOptions = false },
                onToggleFavorite = { onFavoriteToggle(track) },
                onOpenLyrics = { showLyricsSheet = true },
                onOpenEqualizer = { showEqualizerSheet = true },
                onOpenSleepTimer = { showSleepTimerSheet = true }
            )
        }

        // Live Lyrics Bottom Sheet
        if (showLyricsSheet) {
            LyricsBottomSheet(
                track = track,
                playbackState = playbackState,
                sheetState = lyricsSheetState,
                onDismiss = { showLyricsSheet = false },
                onSeekTo = onSeekToMs
            )
        }

        // Equalizer Bottom Sheet
        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                settings = equalizerSettings,
                sheetState = equalizerSheetState,
                onDismiss = { showEqualizerSheet = false },
                onUpdateSettings = onUpdateEqualizer
            )
        }

        // Sleep Timer Bottom Sheet
        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                currentOption = sleepTimerOption,
                remainingSeconds = sleepTimerRemainingSeconds,
                sheetState = sleepTimerSheetState,
                onDismiss = { showSleepTimerSheet = false },
                onSelectOption = onSelectSleepTimer
            )
        }
    }
}
