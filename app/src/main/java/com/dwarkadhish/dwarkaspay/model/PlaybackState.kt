package com.dwarkadhish.dwarkaspay.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isLoading: Boolean = false,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) {
            (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val hasNext: Boolean
        get() = queue.isNotEmpty() && currentQueueIndex < queue.lastIndex

    val hasPrevious: Boolean
        get() = queue.isNotEmpty() && (currentQueueIndex > 0 || playbackPositionMs > 3000L)
}
