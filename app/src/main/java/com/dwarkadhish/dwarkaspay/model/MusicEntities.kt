package com.dwarkadhish.dwarkaspay.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: String = "2026",
    val artworkUri: String? = null,
    val tracksCount: Int = 10,
    val dominantColorHex: Long = 0xFF9B7BFF
)

data class Artist(
    val id: String,
    val name: String,
    val artworkUri: String? = null,
    val followersCount: String = "1.2M",
    val genre: String = "Electronic / Lo-Fi"
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val artworkUri: String? = null,
    val trackIds: List<String> = emptyList(),
    val isUserCreated: Boolean = false,
    val createdDate: String = "Oct 2026"
)
