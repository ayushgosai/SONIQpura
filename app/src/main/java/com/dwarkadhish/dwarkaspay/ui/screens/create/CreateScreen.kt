package com.dwarkadhish.dwarkaspay.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarkadhish.dwarkaspay.data.repository.MusicRepository
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.VioraArtwork
import com.dwarkadhish.dwarkaspay.ui.components.VioraGradientButton
import com.dwarkadhish.dwarkaspay.ui.theme.CardSurfaceVariant
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.DividerColor
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.ErrorRed
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.OutlineColor
import com.dwarkadhish.dwarkaspay.ui.theme.SoftLavender
import com.dwarkadhish.dwarkaspay.ui.theme.TextPrimary
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@Composable
fun CreateScreen(
    musicRepository: MusicRepository,
    allTracks: List<Track>,
    userPlaylists: List<Playlist>,
    onOpenDrawer: () -> Unit,
    onPlaylistCreated: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var selectedArtworkUri by remember {
        mutableStateOf("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80")
    }
    val selectedTrackIds = remember { mutableStateListOf<String>() }

    var editingPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    val presetArtworks = listOf(
        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80"
    )

    fun handleSavePlaylist() {
        if (playlistName.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a playlist name")
            }
            return
        }

        val created = musicRepository.createPlaylist(
            name = playlistName.trim(),
            description = playlistDescription.trim(),
            artworkUri = selectedArtworkUri,
            trackIds = selectedTrackIds.toList()
        )

        // Reset inputs
        playlistName = ""
        playlistDescription = ""
        selectedTrackIds.clear()

        coroutineScope.launch {
            snackbarHostState.showSnackbar("Playlist '${created.title}' created successfully!")
        }
        onPlaylistCreated(created)
    }

    // Delete Confirmation Dialog
    if (playlistToDelete != null) {
        val target = playlistToDelete!!
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = DeepElevatedSurface,
            title = {
                Text("Delete Playlist?", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to delete '${target.title}'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        musicRepository.deletePlaylist(target.id)
                        playlistToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Deleted '${target.title}'")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = MidnightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Edit Playlist Dialog
    if (editingPlaylist != null) {
        val target = editingPlaylist!!
        var editName by remember(target.id) { mutableStateOf(target.title) }
        var editDesc by remember(target.id) { mutableStateOf(target.description) }

        AlertDialog(
            onDismissRequest = { editingPlaylist = null },
            containerColor = DeepElevatedSurface,
            title = {
                Text("Edit Playlist", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Playlist Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = OutlineColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = OutlineColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            musicRepository.updatePlaylist(target.id, editName.trim(), editDesc.trim())
                            editingPlaylist = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Updated '${editName.trim()}'")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Save", color = MidnightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlaylist = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
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
                                tint = SoftLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Create Playlist",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Playlist Name and Details Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "PLAYLIST DETAILS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ElectricViolet
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name") },
                        placeholder = { Text("e.g. Midnight Beats 2026") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = ElectricViolet,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = playlistDescription,
                        onValueChange = { playlistDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Give your playlist a cool description") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = ElectricViolet,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Choose Artwork Style",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(presetArtworks) { uri ->
                            val isSelected = selectedArtworkUri == uri
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) ElectricViolet else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedArtworkUri = uri }
                            ) {
                                VioraArtwork(
                                    artworkUri = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(ElectricViolet),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MidnightBlack,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    VioraGradientButton(
                        text = "Save Playlist",
                        onClick = { handleSavePlaylist() }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Add Songs Section
            item {
                Text(
                    text = "SELECT TRACKS TO ADD (${selectedTrackIds.size} selected)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = ElectricViolet,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(allTracks) { track ->
                val isSelected = selectedTrackIds.contains(track.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background)
                        .clickable {
                            if (isSelected) selectedTrackIds.remove(track.id)
                            else selectedTrackIds.add(track.id)
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VioraArtwork(
                        artworkUri = track.artworkUri,
                        contentDescription = track.title,
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) selectedTrackIds.add(track.id)
                            else selectedTrackIds.remove(track.id)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ElectricViolet,
                            checkmarkColor = MidnightBlack,
                            uncheckedColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // Existing User Playlists Section
            val customPlaylists = userPlaylists.filter { it.isUserCreated }
            if (customPlaylists.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "MANAGE YOUR PLAYLISTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = ElectricViolet,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(customPlaylists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VioraArtwork(
                            artworkUri = playlist.artworkUri,
                            contentDescription = playlist.title,
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${playlist.trackIds.size} songs • ${playlist.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        IconButton(onClick = { editingPlaylist = playlist }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = SoftLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = { playlistToDelete = playlist }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )
    }
}
