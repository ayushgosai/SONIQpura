package com.dwarkadhish.dwarkaspay.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.dwarkadhish.dwarkaspay.data.auth.AuthRepository
import com.dwarkadhish.dwarkaspay.model.AuthState
import com.dwarkadhish.dwarkaspay.data.repository.MusicRepository
import com.dwarkadhish.dwarkaspay.player.AudioPlayerManager
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import com.dwarkadhish.dwarkaspay.ui.components.MiniPlayer
import com.dwarkadhish.dwarkaspay.ui.components.NowPlayingOverlay
import com.dwarkadhish.dwarkaspay.ui.components.ProfileDrawerContent
import com.dwarkadhish.dwarkaspay.ui.screens.auth.ProfileSetupScreen
import com.dwarkadhish.dwarkaspay.ui.screens.profile.UserProfileScreen
import com.dwarkadhish.dwarkaspay.ui.screens.auth.ForgotPasswordScreen
import com.dwarkadhish.dwarkaspay.ui.screens.auth.LoginScreen
import com.dwarkadhish.dwarkaspay.ui.screens.auth.RegisterScreen
import com.dwarkadhish.dwarkaspay.ui.screens.auth.VerifyOtpScreen
import com.dwarkadhish.dwarkaspay.ui.screens.create.CreateScreen
import com.dwarkadhish.dwarkaspay.ui.screens.home.HomeScreen
import com.dwarkadhish.dwarkaspay.ui.screens.library.LibraryScreen
import com.dwarkadhish.dwarkaspay.ui.screens.player.NowPlayingScreen
import com.dwarkadhish.dwarkaspay.ui.screens.player.QueueBottomSheet
import com.dwarkadhish.dwarkaspay.ui.screens.playlist.PlaylistDetailScreen
import com.dwarkadhish.dwarkaspay.ui.screens.premium.PremiumScreen
import com.dwarkadhish.dwarkaspay.ui.screens.search.SearchScreen
import com.dwarkadhish.dwarkaspay.ui.screens.settings.SettingsScreen
import com.dwarkadhish.dwarkaspay.ui.screens.splash.SplashScreen
import com.dwarkadhish.dwarkaspay.VioraApplication
import com.dwarkadhish.dwarkaspay.data.preferences.UserPreferencesManager
import com.dwarkadhish.dwarkaspay.ui.screens.welcome.WelcomeScreen
import com.dwarkadhish.dwarkaspay.ui.theme.DeepElevatedSurface
import com.dwarkadhish.dwarkaspay.ui.theme.ElectricViolet
import com.dwarkadhish.dwarkaspay.ui.theme.MidnightBlack
import com.dwarkadhish.dwarkaspay.ui.theme.TextSecondary
import com.dwarkadhish.dwarkaspay.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VioraNavGraph(
    navController: NavHostController,
    musicRepository: MusicRepository,
    audioPlayerManager: AudioPlayerManager,
    authRepository: AuthRepository,
    userPreferencesManager: UserPreferencesManager = VioraApplication.instance.userPreferencesManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by audioPlayerManager.playbackState.collectAsState()
    val equalizerSettings by audioPlayerManager.equalizerSettings.collectAsState()
    val sleepTimerOption by audioPlayerManager.sleepTimerOption.collectAsState()
    val sleepTimerSecondsLeft by audioPlayerManager.sleepTimerSecondsLeft.collectAsState()
    val allTracks by musicRepository.allTracks.collectAsState()
    val playlists by musicRepository.playlists.collectAsState()
    val albums by musicRepository.albums.collectAsState()
    val favoriteTrackIds by musicRepository.favoriteTrackIds.collectAsState()

    val favoriteTracks = remember(allTracks, favoriteTrackIds) {
        musicRepository.getFavoriteTracks()
    }

    // Full Screen Now Playing Sheet State
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    // Queue Bottom Sheet State
    var isQueueVisible by remember { mutableStateOf(false) }
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val artists by musicRepository.artists.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val authState by authRepository.authState.collectAsState()
    val currentUser = (authState as? AuthState.Authenticated)?.user

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            ProfileDrawerContent(
                user = currentUser,
                onNavigateToSettings = {
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.UserProfile.route)
                },
                onSignOut = {
                    authRepository.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Floating Mini-Player positioned right above bottom navigation bar
                        MiniPlayer(
                            playbackState = playbackState,
                            onMiniPlayerClick = { isNowPlayingExpanded = true },
                            onPlayPauseClick = { audioPlayerManager.playPause() },
                            onNextClick = { audioPlayerManager.skipToNext() },
                            onFavoriteClick = {
                                playbackState.currentTrack?.let {
                                    musicRepository.toggleFavorite(it.id)
                                }
                            }
                        )

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            tonalElevation = 8.dp
                        ) {
                            Screen.bottomNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        screen.icon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = screen.title,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        screen.title?.let { title ->
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Splash Screen - routes from the real auth session state
                composable(Screen.Splash.route) {
                    SplashScreen(
                        authRepository = authRepository,
                        userPreferencesManager = userPreferencesManager,
                        onNavigateToWelcome = {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToVerify = { email ->
                            navController.navigate(Screen.VerifyEmail.createRoute(email)) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Welcome / First Launch Onboarding Screen
                composable(Screen.Welcome.route) {
                    WelcomeScreen(
                        userPreferencesManager = userPreferencesManager,
                        onContinueToAuth = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Login
                composable(Screen.Login.route) {
                    LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Screen.ForgotPassword.route)
                        },
                        onNavigateToVerifyOtp = { email ->
                            navController.navigate(Screen.VerifyEmail.createRoute(email))
                        }
                    )
                }

                // Create Account — validates form, then goes to ProfileSetup
                composable(Screen.Register.route) {
                    RegisterScreen(
                        authRepository = authRepository,
                        onNavigateToProfileSetup = { name, email, password ->
                            // Store name+password in SavedStateHandle for ProfileSetupScreen
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.set("reg_name", name)
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.set("reg_password", password)
                            navController.navigate(Screen.ProfileSetup.createRoute(email))
                        },
                        onGoogleSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Profile Setup — pick photo, birthdate, age, gender
                composable(
                    route = Screen.ProfileSetup.route,
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email").orEmpty()
                    // Retrieve name/password that were saved in Register's backstack entry
                    val previousEntry = navController.previousBackStackEntry
                    val name = previousEntry?.savedStateHandle?.get<String>("reg_name") ?: ""
                    val password = previousEntry?.savedStateHandle?.get<String>("reg_password") ?: ""

                    ProfileSetupScreen(
                        email = email,
                        name = name,
                        password = password,
                        authRepository = authRepository,
                        onProfileSetupComplete = { verifyEmail ->
                            navController.navigate(Screen.VerifyEmail.createRoute(verifyEmail)) {
                                // Pop register + profileSetup off back stack
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Email OTP verification
                composable(
                    route = Screen.VerifyEmail.route,
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email").orEmpty()
                    VerifyOtpScreen(
                        email = email,
                        authRepository = authRepository,
                        onVerificationSuccess = {
                            // After email verified → go straight to Home (clear entire backstack)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateBackToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Forgot password
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        authRepository = authRepository,
                        onNavigateBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Home Screen
                composable(Screen.Home.route) {
                    HomeScreen(
                        tracks = allTracks,
                        playlists = playlists,
                        albums = albums,
                        artists = artists,
                        playbackState = playbackState,
                        user = currentUser,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onTrackClick = { track ->
                            audioPlayerManager.playTrack(track, allTracks)
                        },
                        onPlaylistClick = { playlist ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        },
                        onAlbumClick = { album ->
                            val albumTracks = allTracks.filter { it.album == album.title }
                            if (albumTracks.isNotEmpty()) {
                                audioPlayerManager.setQueueAndPlay(albumTracks, 0)
                            }
                        },
                        onFavoriteToggle = { track ->
                            musicRepository.toggleFavorite(track.id)
                        }
                    )
                }

                // Search Screen
                composable(Screen.Search.route) {
                    SearchScreen(
                        allTracks = allTracks,
                        playlists = playlists,
                        albums = albums,
                        artists = artists,
                        playbackState = playbackState,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onTrackClick = { track, queue ->
                            // Online search results pass their own preview queue so a
                            // 30s preview never auto-jumps into an unrelated local song.
                            audioPlayerManager.playTrack(track, queue)
                        },
                        onPlaylistClick = { playlist ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        },
                        onFavoriteToggle = { track ->
                            musicRepository.toggleFavorite(track.id)
                        }
                    )
                }

                // Library Screen
                composable(Screen.Library.route) {
                    LibraryScreen(
                        playlists = playlists,
                        albums = albums,
                        artists = artists,
                        favoriteTracks = favoriteTracks,
                        playbackState = playbackState,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onPlaylistClick = { playlist ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        },
                        onAlbumClick = { album ->
                            val albumTracks = allTracks.filter { it.album == album.title }
                            if (albumTracks.isNotEmpty()) {
                                audioPlayerManager.setQueueAndPlay(albumTracks, 0)
                            }
                        },
                        onTrackClick = { track ->
                            audioPlayerManager.playTrack(track, allTracks)
                        },
                        onFavoriteToggle = { track ->
                            musicRepository.toggleFavorite(track.id)
                        },
                        onNavigateToCreate = {
                            navController.navigate(Screen.Create.route)
                        },
                        onImportAudioUri = { uri ->
                            coroutineScope.launch {
                                val track = musicRepository.importAudioFromUri(uri)
                                if (track != null) {
                                    audioPlayerManager.playTrack(track, listOf(track) + allTracks)
                                }
                            }
                        },
                        onScanDeviceAudio = {
                            coroutineScope.launch {
                                musicRepository.loadLocalTracks()
                            }
                        }
                    )
                }

                // Premium Screen
                composable(Screen.Premium.route) {
                    PremiumScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }

                // Create Screen
                composable(Screen.Create.route) {
                    CreateScreen(
                        musicRepository = musicRepository,
                        allTracks = allTracks,
                        userPlaylists = playlists,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onPlaylistCreated = { playlist ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        }
                    )
                }

                // Settings Screen
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        authRepository = authRepository,
                        userPreferencesManager = userPreferencesManager,
                        onSignOut = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Playlist Details Screen
                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                    val playlist = playlists.find { it.id == playlistId }
                    val playlistTracks = remember(playlistId, allTracks) {
                        musicRepository.getTracksForPlaylist(playlistId)
                    }

                    if (playlist != null) {
                        PlaylistDetailScreen(
                            playlist = playlist,
                            tracks = playlistTracks,
                            playbackState = playbackState,
                            onBackClick = { navController.popBackStack() },
                            onPlayAllClick = {
                                if (playlistTracks.isNotEmpty()) {
                                    audioPlayerManager.setQueueAndPlay(playlistTracks, 0)
                                }
                            },
                            onShuffleAllClick = {
                                if (playlistTracks.isNotEmpty()) {
                                    audioPlayerManager.setQueueAndPlay(playlistTracks.shuffled(), 0)
                                }
                            },
                            onTrackClick = { track ->
                                audioPlayerManager.playTrack(track, playlistTracks)
                            },
                            onFavoriteToggle = { track ->
                                musicRepository.toggleFavorite(track.id)
                            }
                        )
                    }
                }

                // User Profile Screen
                composable(Screen.UserProfile.route) {
                    UserProfileScreen(
                        user = currentUser,
                        authRepository = authRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ── Dynamic Island-style Now Playing Overlay (top of screen) ──────────
        // Shows only on main bottom-nav screens, hidden when full player is open
        AnimatedVisibility(
            visible = showBottomBar && playbackState.currentTrack != null && !isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(250)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            NowPlayingOverlay(
                playbackState = playbackState,
                onPlayPauseClick = { audioPlayerManager.playPause() },
                onNextClick = { audioPlayerManager.skipToNext() },
                onPreviousClick = { audioPlayerManager.skipToPrevious() },
                onOpenFullPlayer = { isNowPlayingExpanded = true }
            )
        }

        // Expanded Immersive Now Playing Overlay with smooth slide animation
        AnimatedVisibility(
            visible = isNowPlayingExpanded && playbackState.currentTrack != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            NowPlayingScreen(
                playbackState = playbackState,
                equalizerSettings = equalizerSettings,
                sleepTimerOption = sleepTimerOption,
                sleepTimerRemainingSeconds = sleepTimerSecondsLeft,
                onCollapse = { isNowPlayingExpanded = false },
                onPlayPauseClick = { audioPlayerManager.playPause() },
                onNextClick = { audioPlayerManager.skipToNext() },
                onPreviousClick = { audioPlayerManager.skipToPrevious() },
                onSeek = { progress -> audioPlayerManager.seekToProgress(progress) },
                onSeekToMs = { positionMs -> audioPlayerManager.seekTo(positionMs) },
                onShuffleToggle = { audioPlayerManager.toggleShuffle() },
                onRepeatToggle = { audioPlayerManager.toggleRepeatMode() },
                onFavoriteToggle = { track -> musicRepository.toggleFavorite(track.id) },
                onOpenQueue = { isQueueVisible = true },
                onUpdateEqualizer = { settings -> audioPlayerManager.updateEqualizer(settings) },
                onSelectSleepTimer = { option -> audioPlayerManager.setSleepTimer(option) }
            )
        }

        // Queue Bottom Sheet
        if (isQueueVisible) {
            QueueBottomSheet(
                playbackState = playbackState,
                sheetState = queueSheetState,
                onDismiss = { isQueueVisible = false },
                onTrackSelect = { track ->
                    audioPlayerManager.playTrack(track, playbackState.queue)
                },
                onFavoriteToggle = { track ->
                    musicRepository.toggleFavorite(track.id)
                }
            )
        }
    }
}
}
