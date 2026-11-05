package com.dwarkadhish.dwarkaspay.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.TrackRowItem
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    playbackState: PlaybackState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onTrackSelect: (Track) -> Unit,
    onFavoriteToggle: (Track) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepElevatedSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OutlineColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playing Queue",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${playbackState.queue.size} songs in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
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

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                itemsIndexed(playbackState.queue) { index, track ->
                    val isPlaying = playbackState.currentTrack?.id == track.id
                    TrackRowItem(
                        track = track,
                        isPlaying = isPlaying,
                        onTrackClick = {
                            onTrackSelect(track)
                            onDismiss()
                        },
                        onFavoriteClick = { onFavoriteToggle(track) }
                    )
                }
            }
        }
    }
}
