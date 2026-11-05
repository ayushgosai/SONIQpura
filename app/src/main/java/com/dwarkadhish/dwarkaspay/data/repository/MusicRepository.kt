package com.dwarkadhish.dwarkaspay.data.repository

import com.dwarkadhish.dwarkaspay.data.local.LocalAudioScanner
import com.dwarkadhish.dwarkaspay.data.local.SampleMusicCatalog
import com.dwarkadhish.dwarkaspay.model.Album
import com.dwarkadhish.dwarkaspay.model.Artist
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class MusicRepository(
    private val localAudioScanner: LocalAudioScanner
) {
    private val _allTracks = MutableStateFlow<List<Track>>(SampleMusicCatalog.sampleTracks)
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(SampleMusicCatalog.samplePlaylists)
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(SampleMusicCatalog.sampleAlbums)
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(SampleMusicCatalog.sampleArtists)
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _favoriteTrackIds = MutableStateFlow<Set<String>>(
        SampleMusicCatalog.sampleTracks.filter { it.isFavorite }.map { it.id }.toSet()
    )
    val favoriteTrackIds: StateFlow<Set<String>> = _favoriteTrackIds.asStateFlow()

    suspend fun loadLocalTracks() {
        val scanned = localAudioScanner.scanLocalTracks()
        if (scanned.isNotEmpty()) {
            _allTracks.update { existing ->
                val combined = (existing + scanned).distinctBy { it.id }
                combined
            }
        }
    }

    suspend fun importAudioFromUri(uri: android.net.Uri): Track? {
        val track = localAudioScanner.parseTrackFromUri(uri)
        if (track != null) {
            _allTracks.update { existing ->
                (listOf(track) + existing).distinctBy { it.id }
            }
        }
        return track
    }

    fun toggleFavorite(trackId: String) {
        _favoriteTrackIds.update { set ->
            if (set.contains(trackId)) {
                set - trackId
            } else {
                set + trackId
            }
        }
        _allTracks.update { list ->
            list.map {
                if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    fun searchTracks(query: String): List<Track> {
        if (query.isBlank()) return _allTracks.value
        val lower = query.trim().lowercase()
        return _allTracks.value.filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower) ||
            it.genre.lowercase().contains(lower)
        }
    }

    fun getTracksForPlaylist(playlistId: String): List<Track> {
        val playlist = _playlists.value.find { it.id == playlistId } ?: return emptyList()
        val trackMap = _allTracks.value.associateBy { it.id }
        return playlist.trackIds.mapNotNull { trackMap[it] }
    }

    fun getFavoriteTracks(): List<Track> {
        val favIds = _favoriteTrackIds.value
        return _allTracks.value.filter { favIds.contains(it.id) }
    }

    fun createPlaylist(name: String, description: String = "", artworkUri: String? = null, trackIds: List<String> = emptyList()): Playlist {
        val newPlaylist = Playlist(
            id = "pl_${UUID.randomUUID().toString().take(8)}",
            title = name,
            description = description.ifBlank { "Curated by you in SONIQ Music." },
            artworkUri = artworkUri ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            trackIds = trackIds,
            isUserCreated = true
        )
        _playlists.update { it + newPlaylist }
        return newPlaylist
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.update { list -> list.filter { it.id != playlistId } }
    }

    fun updatePlaylist(playlistId: String, name: String, description: String) {
        _playlists.update { list ->
            list.map {
                if (it.id == playlistId) it.copy(title = name, description = description) else it
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        _playlists.update { list ->
            list.map {
                if (it.id == playlistId && !it.trackIds.contains(trackId)) {
                    it.copy(trackIds = it.trackIds + trackId)
                } else {
                    it
                }
            }
        }
    }
}
