package com.dwarkadhish.dwarkaspay

import android.app.Application
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.data.local.LocalAudioScanner
import com.dwarkadhish.dwarkaspay.data.repository.MusicRepository
import com.dwarkadhish.dwarkaspay.player.AudioPlayerManager

class VioraApplication : Application() {

    lateinit var localAudioScanner: LocalAudioScanner
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var audioPlayerManager: AudioPlayerManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var userPreferencesManager: com.dwarkadhish.dwarkaspay.data.preferences.UserPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("VioraApplication", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        userPreferencesManager = com.dwarkadhish.dwarkaspay.data.preferences.UserPreferencesManager(this)
        localAudioScanner = LocalAudioScanner(this)
        musicRepository = MusicRepository(localAudioScanner)
        audioPlayerManager = AudioPlayerManager(this)
        authRepository = AuthRepository(this)
    }

    companion object {
        lateinit var instance: VioraApplication
            private set
    }
}
