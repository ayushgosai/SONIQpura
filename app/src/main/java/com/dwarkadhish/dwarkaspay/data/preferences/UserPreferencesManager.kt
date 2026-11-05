package com.dwarkadhish.dwarkaspay.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("viora_user_preferences", Context.MODE_PRIVATE)

    private val _themeModeFlow = kotlinx.coroutines.flow.MutableStateFlow(themeMode)
    val themeModeFlow: kotlinx.coroutines.flow.StateFlow<String> = _themeModeFlow

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_LIGHT) ?: THEME_LIGHT
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
            _themeModeFlow.value = value
        }

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var hasRequestedNotificationPermission: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_REQUESTED, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_REQUESTED, value).apply()

    var isHighQualityAudioEnabled: Boolean
        get() = prefs.getBoolean(KEY_HIGH_QUALITY_AUDIO, true)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_QUALITY_AUDIO, value).apply()

    var isGaplessPlaybackEnabled: Boolean
        get() = prefs.getBoolean(KEY_GAPLESS_PLAYBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_GAPLESS_PLAYBACK, value).apply()

    companion object {
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
        const val THEME_SYSTEM = "SYSTEM"

        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_NOTIFICATION_REQUESTED = "key_notification_requested"
        private const val KEY_HIGH_QUALITY_AUDIO = "key_high_quality_audio"
        private const val KEY_GAPLESS_PLAYBACK = "key_gapless_playback"
    }
}
