package com.dwarkadhish.dwarkaspay.model

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val presetName: String = "Midnight Bass",
    val bandLevels: List<Int> = listOf(6, 4, 1, 3, 5), // dB range: -10 to +10
    val bassBoost: Int = 70, // 0 to 100%
    val virtualizer: Int = 50 // 0 to 100%
)

enum class SleepTimerOption(val label: String, val minutes: Int) {
    OFF("Turn off", 0),
    MIN_15("15 minutes", 15),
    MIN_30("30 minutes", 30),
    MIN_45("45 minutes", 45),
    MIN_60("1 hour", 60),
    END_OF_TRACK("End of track", -1)
}
