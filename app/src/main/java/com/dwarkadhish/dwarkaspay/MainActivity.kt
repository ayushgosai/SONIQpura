package com.dwarkadhish.dwarkaspay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dwarkadhish.dwarkaspay.ui.navigation.VioraNavGraph
import com.dwarkadhish.dwarkaspay.ui.theme.VioraMusicTheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.dwarkadhish.dwarkaspay.data.preferences.UserPreferencesManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS permission on Android 13+ (Tiramisu, API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        val app = application as VioraApplication
        val musicRepo = app.musicRepository
        val playerManager = app.audioPlayerManager
        val prefs = app.userPreferencesManager

        // Asynchronously load local music files from device into repository
        lifecycleScope.launch {
            try {
                musicRepo.loadLocalTracks()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to load local tracks", e)
            }
        }

        setContent {
            val themeMode by prefs.themeModeFlow.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                UserPreferencesManager.THEME_LIGHT -> false
                UserPreferencesManager.THEME_DARK -> true
                else -> systemDark
            }

            VioraMusicTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                VioraNavGraph(
                    navController = navController,
                    musicRepository = musicRepo,
                    audioPlayerManager = playerManager,
                    authRepository = app.authRepository,
                    userPreferencesManager = prefs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}