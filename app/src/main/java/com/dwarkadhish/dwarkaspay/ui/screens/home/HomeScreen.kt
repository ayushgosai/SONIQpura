package com.dwarkadhish.dwarkaspay.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.model.Album
import com.dwarkadhish.dwarkaspay.model.Artist
import com.dwarkadhish.dwarkaspay.model.AuthUser
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.AlbumCard
import com.dwarkadhish.dwarkaspay.ui.components.VioraArtwork
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary
import java.util.Calendar

@Composable
fun HomeScreen(
    tracks: List<Track>,
    playlists: List<Playlist>,
    albums: List<Album>,
    artists: List<Artist>,
    playbackState: PlaybackState,
    user: AuthUser?,
    onOpenDrawer: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAlbumClick: (Album) -> Unit = {},
    onFavoriteToggle: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Music", "Podcasts")

    // Dynamic greeting calculation
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Late Night Vibes"
        }
    }

    // Quick access items: pair of tracks and playlists
    val quickAccessItems = remember(tracks, playlists) {
        val list = mutableListOf<QuickAccessData>()
        playlists.take(3).forEach {
            list.add(QuickAccessData(id = it.id, title = it.title, artworkUri = it.artworkUri, isPlaylist = true, playlist = it))
        }
        tracks.take(3).forEach {
            list.add(QuickAccessData(id = it.id, title = it.title, artworkUri = it.artworkUri, isPlaylist = false, track = it))
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Area: Profile Avatar + Greeting + Icons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val photoUri = user?.profilePhotoUri ?: user?.avatarUrl
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(ElectricViolet, SoftLavender, ElectricViolet)
                                ),
                                shape = CircleShape
                            )
                            .clickable { onOpenDrawer() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            val initial = user?.displayName?.take(1)?.uppercase()
                            if (!initial.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ElectricViolet.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ElectricViolet
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ElectricViolet.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Profile Drawer",
                                        tint = ElectricViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (user != null) {
                            Text(
                                text = user.displayName ?: "SONIQ Listener",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = "Updates",
                            tint = SoftLavender,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Recents",
                            tint = SoftLavender,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Filter Chips: All, Music, Podcasts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DeepElevatedSurface,
                            labelColor = TextSecondary,
                            selectedContainerColor = ElectricViolet,
                            selectedLabelColor = MidnightBlack
                        ),
                        border = null,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Quick Access 2-Column Grid (6 items)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                val chunked = quickAccessItems.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            QuickAccessCard(
                                item = item,
                                onClick = {
                                    if (item.isPlaylist && item.playlist != null) {
                                        onPlaylistClick(item.playlist)
                                    } else if (item.track != null) {
                                        onTrackClick(item.track)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Carousel 1: Recommended for Today
        item {
            SectionHeader(title = "Recommended for Today")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tracks) { track ->
                    SquareMusicCard(
                        artworkUri = track.artworkUri,
                        title = track.title,
                        subtitle = track.artist,
                        isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Carousel 2: Made For You (Playlists)
        item {
            SectionHeader(title = "Made For You", subtitle = "Curated mixes based on your taste")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists) { playlist ->
                    SquareMusicCard(
                        artworkUri = playlist.artworkUri,
                        title = playlist.title,
                        subtitle = "${playlist.trackIds.size} songs • Playlist",
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Carousel 3: Popular Artists (Circular Cards)
        item {
            SectionHeader(title = "Popular Artists")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(artists) { artist ->
                    CircularArtistCard(
                        artist = artist,
                        onClick = {}
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Carousel 4: New Releases (Albums)
        item {
            SectionHeader(title = "New Releases & Albums")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    AlbumCard(
                        title = album.title,
                        subtitle = "Album • ${album.artist}",
                        artworkUri = album.artworkUri,
                        onClick = { onAlbumClick(album) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Carousel 5: Recently Played
        item {
            SectionHeader(title = "Recently Played")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tracks.reversed()) { track ->
                    SquareMusicCard(
                        artworkUri = track.artworkUri,
                        title = track.title,
                        subtitle = track.genre,
                        isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

data class QuickAccessData(
    val id: String,
    val title: String,
    val artworkUri: String?,
    val isPlaylist: Boolean,
    val track: Track? = null,
    val playlist: Playlist? = null
)

@Composable
private fun QuickAccessCard(
    item: QuickAccessData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VioraArtwork(
            artworkUri = item.artworkUri,
            contentDescription = item.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SquareMusicCard(
    artworkUri: String?,
    title: String,
    subtitle: String,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            VioraArtwork(
                artworkUri = artworkUri,
                contentDescription = title,
                modifier = Modifier.fillMaxSize()
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MidnightBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CircularArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VioraArtwork(
            artworkUri = artist.artworkUri,
            contentDescription = artist.name,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Artist",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
