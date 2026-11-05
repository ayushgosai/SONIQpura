package com.dwarkadhish.dwarkaspay.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class TrackLyrics(
    val trackId: String,
    val lines: List<LyricLine>
)
