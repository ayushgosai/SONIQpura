package com.dwarkadhish.dwarkaspay.ui.screens.search

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dwarkadhish.dwarkaspay.data.network.ItunesClient
import com.dwarkadhish.dwarkaspay.data.network.ItunesTrack
import com.dwarkadhish.dwarkaspay.data.network.RetrofitClient
import com.dwarkadhish.dwarkaspay.data.network.SaavnSongDto
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track
import com.dwarkadhish.dwarkaspay.ui.components.VioraArtwork
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GenreTile(val title: String, val gradient: Brush)

data class OnlineSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val durationMs: Long,
    val previewUrl: String? = null,
    val streamUrl: String? = null
)

@Composable
fun SearchScreen(
    allTracks: List<Track>,
    playlists: List<Playlist>,
    albums: List<com.dwarkadhish.dwarkaspay.model.Album>,
    artists: List<com.dwarkadhish.dwarkaspay.model.Artist>,
    playbackState: PlaybackState,
    onOpenDrawer: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val searchHistory = remember { mutableStateListOf<String>() }

    // Online search state
    var onlineResults by remember { mutableStateOf<List<OnlineSong>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var useFullSongs by remember { mutableStateOf(true) }
    var resultSource by remember { mutableStateOf("FULL SONGS • IN-APP") }
    var retryTick by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Manual server override — demo ke time laptop ka IP badal jaye to rebuild ki zarurat nahi.
    val prefs = remember { context.getSharedPreferences("viora_prefs", Context.MODE_PRIVATE) }
    val savedServer = remember { prefs.getString("server_base_url", null) }
    var serverInput by remember { mutableStateOf(savedServer.orEmpty()) }

    LaunchedEffect(Unit) {
        if (!savedServer.isNullOrBlank()) RetrofitClient.setOverrideBaseUrl(savedServer)
    }

    val categories = listOf("All", "Music", "Podcasts", "Live Events")

    val genreTiles = remember {
        listOf(
            GenreTile("Bollywood", Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE0979)))),
            GenreTile("Lo-Fi & Chill", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))),
            GenreTile("Pop", Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))),
            GenreTile("Hip-Hop", Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
            GenreTile("Rock", Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))),
            GenreTile("Electronic", Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))),
            GenreTile("Punjabi", Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF9333EA)))),
            GenreTile("Sleep & Ambient", Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0F766E)))),
            GenreTile("Workout", Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFC2410C)))),
            GenreTile("Classical", Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4338CA))))
        )
    }

    // Debounced online search — full songs stream in-app, or 30s previews.
    LaunchedEffect(searchQuery, useFullSongs, retryTick) {
        debounceJob?.cancel()
        if (searchQuery.isBlank()) {
            onlineResults = emptyList()
            isLoading = false
            searchError = null
            return@LaunchedEffect
        }
        debounceJob = coroutineScope.launch {
            delay(400)
            isLoading = true
            searchError = null
            val query = searchQuery.trim()

            if (useFullSongs) {
                try {
                    val response = RetrofitClient.saavnSearch(query, limit = 30)
                    onlineResults = response.songs.map { it.toOnlineSong() }
                    resultSource = "FULL SONGS • IN-APP"
                } catch (_: Exception) {
                    // Never silently fall back to 30s previews: those clips start
                    // mid-song, which looks like a bug. Ask the user instead.
                    onlineResults = emptyList()
                    searchError = "Can't reach the Viora server. Start the backend on your laptop, then enter your laptop IP below and tap Save & Retry."
                } finally {
                    isLoading = false
                }
                return@launch
            }

            try {
                val response = ItunesClient.api.search(
                    term = query,
                    limit = 30,
                    country = "IN"
                )
                onlineResults = response.results.filter { it.kind == "song" }.map { it.toOnlineSong() }
                resultSource = "30s PREVIEWS"
            } catch (e: Exception) {
                searchError = "No internet connection. Turn on Wi-Fi or mobile data and try again."
                onlineResults = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    val isSearching = searchQuery.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ── Search Input Bar ─────────────────────────────────────────────────
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Songs, artists, albums...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor     = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor   = MaterialTheme.colorScheme.outline,
                    // ✅ KEY FIX: explicit text colors so visible in both light & dark
                    focusedTextColor       = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor     = MaterialTheme.colorScheme.onSurface,
                    cursorColor            = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── Source Toggle: full songs in-app vs 30s previews ─────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = useFullSongs,
                    onClick = { useFullSongs = true },
                    label = {
                        Text(
                            text = "Full Songs",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (useFullSongs) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (useFullSongs) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null,
                    shape = RoundedCornerShape(20.dp)
                )
                FilterChip(
                    selected = !useFullSongs,
                    onClick = { useFullSongs = false },
                    label = {
                        Text(
                            text = "30s Preview",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (!useFullSongs) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (!useFullSongs) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null,
                    shape = RoundedCornerShape(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Category Filter Chips ────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = Color.White,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null,
                        shape  = RoundedCornerShape(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ── NOT SEARCHING: History + Genre Grid ─────────────────────────────
        if (!isSearching) {
            if (searchHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT SEARCHES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Clear all",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { searchHistory.clear() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(searchHistory) { queryItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { searchQuery = queryItem }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = queryItem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { searchHistory.remove(queryItem) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(18.dp)) }
            }

            item {
                Text(
                    text = "BROWSE ALL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val chunked = genreTiles.chunked(2)
            items(chunked) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tile ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(tile.gradient)
                                .clickable {
                                    searchQuery = tile.title
                                    if (!searchHistory.contains(tile.title))
                                        searchHistory.add(0, tile.title)
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = tile.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        } else {
            // ── SEARCHING: Online Results ─────────────────────────────────────
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Searching online...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                searchError != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 28.dp, end = 28.dp, top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = searchError!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = serverInput,
                                onValueChange = { serverInput = it },
                                label = { Text("Server address") },
                                placeholder = { Text("http://10.113.155.164:3000") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    prefs.edit()
                                        .putString("server_base_url", serverInput.trim())
                                        .apply()
                                    RetrofitClient.setOverrideBaseUrl(serverInput)
                                    retryTick++
                                },
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save & Retry")
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(onClick = { useFullSongs = false }) {
                                Text("Use 30s Preview instead")
                            }
                        }
                    }
                }

                onlineResults.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try a different song, artist or album.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    item {
                        Text(
                            text = "${onlineResults.size} RESULTS • $resultSource",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    items(onlineResults) { song ->
                        OnlineTrackRow(
                            track = song,
                            onClick = {
                                // Save to history
                                val q = searchQuery.trim()
                                if (q.isNotBlank() && !searchHistory.contains(q))
                                    searchHistory.add(0, q)

                                // Play inside Viora: the queue only contains the
                                // results that are actually playable.
                                val queue = onlineResults
                                    .filter { it.isPlayable() }
                                    .map { it.toLocalTrack() }
                                onTrackClick(song.toLocalTrack(), queue)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Online Track Row ─────────────────────────────────────────────────────────

@Composable
private fun OnlineTrackRow(
    track: OnlineSong,
    onClick: () -> Unit
) {
    val isFull = !track.streamUrl.isNullOrBlank()
    val isPlayable = isFull || !track.previewUrl.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isPlayable, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork
        val artUrl = track.artworkUrl
        if (artUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(track.artist)
                    if (track.album.isNotBlank()) {
                        append(" • ")
                        append(track.album)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isPlayable) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isFull) "FULL" else "PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isFull) formatDuration(track.durationMs) else "0:30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.PlayCircle,
                contentDescription = if (isFull) "Play full song" else "Play preview",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Text(
                text = "N/A",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ── Mappers ──────────────────────────────────────────────────────────────────

private const val PREVIEW_DURATION_MS = 30_000L

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(java.util.Locale.getDefault(), "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}

fun ItunesTrack.toOnlineSong(): OnlineSong = OnlineSong(
    id = "itunes_${trackId ?: System.currentTimeMillis()}",
    title = trackName ?: "Unknown Title",
    artist = artistName ?: "Unknown Artist",
    album = collectionName ?: "",
    artworkUrl = artworkUrl100?.replace("100x100", "300x300"),
    durationMs = if (!previewUrl.isNullOrBlank()) PREVIEW_DURATION_MS else 0L,
    previewUrl = previewUrl
)

fun SaavnSongDto.toOnlineSong(): OnlineSong = OnlineSong(
    id = "saavn_$id",
    title = title.ifBlank { "Unknown Title" },
    artist = artist.ifBlank { "Unknown Artist" },
    album = album,
    artworkUrl = image.ifBlank { null },
    durationMs = durationMs,
    streamUrl = streamUrl
)

fun OnlineSong.isPlayable(): Boolean =
    !streamUrl.isNullOrBlank() || !previewUrl.isNullOrBlank()

fun OnlineSong.toLocalTrack(): Track = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    // Preview clips are exactly 30 seconds; full songs use their real duration.
    durationMs = if (!streamUrl.isNullOrBlank()) durationMs else PREVIEW_DURATION_MS,
    mediaUri = streamUrl?.takeIf { it.isNotBlank() } ?: previewUrl ?: "",
    artworkUri = artworkUrl,
    genre = "",
    isFavorite = false
)

