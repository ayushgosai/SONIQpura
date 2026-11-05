package com.dwarkadhish.dwarkaspay.data.local

import com.dwarkadhish.dwarkaspay.model.Album
import com.dwarkadhish.dwarkaspay.model.Artist
import com.dwarkadhish.dwarkaspay.model.Playlist
import com.dwarkadhish.dwarkaspay.model.Track

object SampleMusicCatalog {

    val sampleTracks = listOf(
        Track(
            id = "track_1",
            title = "Midnight City Echoes",
            artist = "Aura Violet",
            album = "Midnight Horizons",
            durationMs = 224000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            artworkUri = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80",
            genre = "Midnight Lo-Fi",
            isFavorite = true
        ),
        Track(
            id = "track_2",
            title = "Electric Lavender",
            artist = "Kaelen & Nova",
            album = "Lavender Dreams",
            durationMs = 285000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            artworkUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            genre = "Synthwave",
            isFavorite = true
        ),
        Track(
            id = "track_3",
            title = "Velvet Skyline",
            artist = "Synthetix",
            album = "Neon & Shadows",
            durationMs = 247000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            artworkUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            genre = "Chillwave",
            isFavorite = false
        ),
        Track(
            id = "track_4",
            title = "Afterhours in Kyoto",
            artist = "Hana & The Beats",
            album = "Midnight Horizons",
            durationMs = 210000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            artworkUri = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            genre = "Lo-Fi Beats",
            isFavorite = true
        ),
        Track(
            id = "track_5",
            title = "Deep Space Pulse",
            artist = "Cosmo Void",
            album = "Orbit 9",
            durationMs = 312000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            artworkUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            genre = "Ambient Electronic",
            isFavorite = false
        ),
        Track(
            id = "track_6",
            title = "Violet Starlight",
            artist = "Luna Eclipse",
            album = "Lavender Dreams",
            durationMs = 198000L,
            mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            artworkUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            genre = "Deep Chill",
            isFavorite = false
        )
    )

    val samplePlaylists = listOf(
        Playlist(
            id = "pl_1",
            title = "Midnight Chill & Focus",
            description = "Subtle deep beats, velvet synths and soothing violet melodies for night sessions.",
            artworkUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            trackIds = listOf("track_1", "track_2", "track_4"),
            isUserCreated = false
        ),
        Playlist(
            id = "pl_2",
            title = "Electric Violet Vibes",
            description = "High energy melodic synthwave and pulsating basslines.",
            artworkUri = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80",
            trackIds = listOf("track_2", "track_3", "track_6"),
            isUserCreated = false
        ),
        Playlist(
            id = "pl_3",
            title = "Late Night Coding",
            description = "Instrumental rhythms engineered to keep you in the zone.",
            artworkUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            trackIds = listOf("track_1", "track_3", "track_5"),
            isUserCreated = true
        )
    )

    val sampleAlbums = listOf(
        Album(
            id = "album_1",
            title = "Midnight Horizons",
            artist = "Aura Violet",
            year = "2026",
            artworkUri = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&auto=format&fit=crop&q=80",
            tracksCount = 8
        ),
        Album(
            id = "album_2",
            title = "Lavender Dreams",
            artist = "Kaelen & Nova",
            year = "2026",
            artworkUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            tracksCount = 6
        ),
        Album(
            id = "album_3",
            title = "Neon & Shadows",
            artist = "Synthetix",
            year = "2025",
            artworkUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            tracksCount = 10
        )
    )

    val sampleArtists = listOf(
        Artist(
            id = "art_1",
            name = "Aura Violet",
            artworkUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
            followersCount = "1.8M",
            genre = "Midnight Lo-Fi"
        ),
        Artist(
            id = "art_2",
            name = "Kaelen & Nova",
            artworkUri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80",
            followersCount = "940K",
            genre = "Synthwave"
        ),
        Artist(
            id = "art_3",
            name = "Synthetix",
            artworkUri = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80",
            followersCount = "2.3M",
            genre = "Chillwave"
        )
    )
}
