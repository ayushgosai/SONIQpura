package com.dwarkadhish.dwarkaspay.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.model.Album
import com.dwarkadhish.dwarkaspay.model.Artist
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.VioraArtwork
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricVioletGradient
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.FolderOpen
import com.dwarkadhish.dwarkaspay.ui.components.PermissionExplanationDialog
import com.dwarkadhish.dwarkaspay.ui.components.PermissionPermanentlyDeniedDialog
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    playlists: List<Playlist>,
    albums: List<Album>,
    artists: List<Artist>,
    favoriteTracks: List<Track>,
    playbackState: PlaybackState,
    onOpenDrawer: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAlbumClick: (Album) -> Unit = {},
    onTrackClick: (Track) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onNavigateToCreate: () -> Unit,
    onImportAudioUri: (Uri) -> Unit = {},
    onScanDeviceAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Playlists", "Artists", "Albums")

    var showScanExplanationDialog by remember { mutableStateOf(false) }
    var showPermanentlyDenied by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportAudioUri(it) }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanDeviceAudio()
        } else {
            showPermanentlyDenied = true
        }
    }

    var isGridView by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var librarySearchQuery by remember { mutableStateOf("") }

    val filteredPlaylists = remember(selectedFilter, librarySearchQuery, playlists) {
        if (selectedFilter != "All" && selectedFilter != "Playlists") emptyList()
        else {
            if (librarySearchQuery.isBlank()) playlists
            else playlists.filter { it.title.contains(librarySearchQuery, ignoreCase = true) }
        }
    }

    val filteredArtists = remember(selectedFilter, librarySearchQuery, artists) {
        if (selectedFilter != "All" && selectedFilter != "Artists") emptyList()
        else {
            if (librarySearchQuery.isBlank()) artists
            else artists.filter { it.name.contains(librarySearchQuery, ignoreCase = true) }
        }
    }

    val filteredAlbums = remember(selectedFilter, librarySearchQuery, albums) {
        if (selectedFilter != "All" && selectedFilter != "Albums") emptyList()
        else {
            if (librarySearchQuery.isBlank()) albums
            else albums.filter { it.title.contains(librarySearchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search Library",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = onNavigateToCreate) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Playlist",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Search Input (Collapsible)
        if (isSearchExpanded) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = librarySearchQuery,
                        onValueChange = { librarySearchQuery = it },
                        placeholder = { Text("Search in Your Library", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        ),
                        border = null,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Local Music & Device Import Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Pick Audio File (System Picker - 0 broad permissions!)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { audioPickerLauncher.launch("audio/*") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AudioFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pick Audio",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Button 2: Scan Device Music (With contextual explanation)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showScanExplanationDialog = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Device",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sorting & Layout Toggle Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Toggle View",
                        tint = SoftLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Pinned Liked Songs Card (Always at the top)
        if (selectedFilter == "All" || selectedFilter == "Playlists") {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (favoriteTracks.isNotEmpty()) {
                                onTrackClick(favoriteTracks.first())
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFF4C1D95))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Liked Songs",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Liked Songs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pinned • ${favoriteTracks.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftLavender
                            )
                        }
                    }
                }
            }
        }

        // Playlists List
        items(filteredPlaylists) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VioraArtwork(
                    artworkUri = playlist.artworkUri,
                    contentDescription = playlist.title,
                    modifier = Modifier.size(62.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Playlist • ${playlist.trackIds.size} songs • ${if (playlist.isUserCreated) "You" else "Viora"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Albums List
        items(filteredAlbums) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumClick(album) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VioraArtwork(
                    artworkUri = album.artworkUri,
                    contentDescription = album.title,
                    modifier = Modifier.size(62.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Album • ${album.artist} • ${album.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Artists List
        items(filteredArtists) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VioraArtwork(
                    artworkUri = artist.artworkUri,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Artist • ${artist.followersCount} followers",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftLavender
                    )
                }
            }
        }
    }

    if (showScanExplanationDialog) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        PermissionExplanationDialog(
            icon = Icons.Rounded.FolderOpen,
            title = "Scan Device Music",
            description = "Viora Music needs permission to scan your device audio files and display them inside your library.",
            benefits = listOf(
                "Access songs already saved on your phone",
                "Full album artwork and metadata support",
                "No files are modified or uploaded to the cloud"
            ),
            confirmButtonText = "Grant Permission",
            dismissButtonText = "Cancel",
            onConfirm = {
                showScanExplanationDialog = false
                storagePermissionLauncher.launch(permission)
            },
            onDismiss = {
                showScanExplanationDialog = false
            }
        )
    }

    if (showPermanentlyDenied) {
        PermissionPermanentlyDeniedDialog(
            title = "Storage Access Denied",
            description = "To browse your entire device audio library, please allow audio access in Android App Settings. Alternatively, you can use 'Pick Audio' to choose individual songs without granting broad storage permissions.",
            onDismiss = { showPermanentlyDenied = false }
        )
    }
}
