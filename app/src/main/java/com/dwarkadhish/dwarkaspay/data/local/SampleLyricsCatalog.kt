package com.dwarkadhish.dwarkaspay.data.local

import com.dwarkadhish.dwarkaspay.model.LyricLine
import com.dwarkadhish.dwarkaspay.model.TrackLyrics

object SampleLyricsCatalog {

    fun getLyricsForTrack(trackId: String): TrackLyrics {
        val lines = when (trackId) {
            "track_1" -> listOf(
                LyricLine(0L, "♪ (Intro - Ambient Synthwave Waves) ♪"),
                LyricLine(8000L, "Walking through the midnight haze"),
                LyricLine(16000L, "Neon lights in purple maze"),
                LyricLine(24000L, "Hear the city start to breathe"),
                LyricLine(32000L, "All the secrets underneath"),
                LyricLine(42000L, "Echoes floating in the rain"),
                LyricLine(50000L, "Washing away yesterday's pain"),
                LyricLine(60000L, "Midnight vibes, electric glow"),
                LyricLine(70000L, "Let the rhythm take control"),
                LyricLine(82000L, "♪ (Synth Drop & Bassline) ♪"),
                LyricLine(100000L, "Shadows dance across the street"),
                LyricLine(112000L, "Syncing with the heartbeat beat"),
                LyricLine(126000L, "Electric sky, obsidian night"),
                LyricLine(140000L, "Guided by the violet light"),
                LyricLine(160000L, "Midnight city echoes high"),
                LyricLine(180000L, "Reaching out into the sky"),
                LyricLine(200000L, "♪ (Outro - Fade to Silence) ♪")
            )
            "track_2" -> listOf(
                LyricLine(0L, "♪ (Soft Piano & Lavender Drone) ♪"),
                LyricLine(10000L, "Caught inside an electric haze"),
                LyricLine(22000L, "Dreaming of the lavender days"),
                LyricLine(35000L, "Soft frequencies fill the room"),
                LyricLine(48000L, "Chasing away all the gloom"),
                LyricLine(62000L, "Violet horizons begin to shine"),
                LyricLine(75000L, "Frozen moments, out of time"),
                LyricLine(92000L, "♪ (Dreamy Chorus Melodies) ♪"),
                LyricLine(115000L, "Electric touch, gentle sound"),
                LyricLine(130000L, "Feet lifted from the ground"),
                LyricLine(150000L, "Lavender skies everywhere"),
                LyricLine(175000L, "Feel the magic in the air"),
                LyricLine(205000L, "♪ (Ambient Outro) ♪")
            )
            "track_3" -> listOf(
                LyricLine(0L, "♪ (Chillwave Beat intro) ♪"),
                LyricLine(12000L, "Velvet skies above the tower"),
                LyricLine(25000L, "Lost inside the witching hour"),
                LyricLine(40000L, "Synthesizer in my head"),
                LyricLine(55000L, "Words that never needed said"),
                LyricLine(70000L, "Cool breeze blowing from the bay"),
                LyricLine(90000L, "Watch the shadows fade away"),
                LyricLine(110000L, "♪ (Guitar Riff & Chill Chords) ♪"),
                LyricLine(135000L, "Skyline glowing deep and wide"),
                LyricLine(160000L, "Rolling with the midnight tide"),
                LyricLine(190000L, "♪ (Outro) ♪")
            )
            else -> listOf(
                LyricLine(0L, "♪ (Instrumental Melody) ♪"),
                LyricLine(12000L, "Feel the rhythm of the night"),
                LyricLine(24000L, "Surrounded by the violet light"),
                LyricLine(38000L, "Immerse into the soundscape deep"),
                LyricLine(52000L, "Awakening from midnight sleep"),
                LyricLine(70000L, "♪ (Harmonic Groove) ♪"),
                LyricLine(95000L, "Viora sound flows through your mind"),
                LyricLine(120000L, "Leaving every rush behind"),
                LyricLine(150000L, "♪ (Smooth Ambient Fade) ♪")
            )
        }
        return TrackLyrics(trackId = trackId, lines = lines)
    }
}
