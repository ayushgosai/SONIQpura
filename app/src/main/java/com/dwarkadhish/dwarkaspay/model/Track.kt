package com.dwarkadhish.dwarkaspay.model

import java.util.Locale

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val genre: String = "Midnight Lo-Fi",
    val isFavorite: Boolean = false
) {
    fun formattedDuration(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, remainingSeconds)
    }
}
