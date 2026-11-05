package com.dwarkadhish.dwarkaspay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")

    // ---- Authentication Flow ----
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ProfileSetup : Screen("profile_setup/{email}") {
        fun createRoute(email: String): String = "profile_setup/$email"
    }
    data object VerifyEmail : Screen("verify_email/{email}") {
        fun createRoute(email: String): String = "verify_email/$email"
    }
    data object ForgotPassword : Screen("forgot_password")

    // ---- Main 5 Bottom Nav Destinations ----
    data object Home : Screen("home", "Home", Icons.Rounded.Home)
    data object Search : Screen("search", "Search", Icons.Rounded.Search)
    data object Library : Screen("library", "Your Library", Icons.Rounded.LibraryMusic)
    data object Premium : Screen("premium", "Premium", Icons.Rounded.WorkspacePremium)
    data object Create : Screen("create", "Create", Icons.Rounded.AddCircleOutline)

    // Secondary Protected Screens
    data object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    data object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: String): String = "playlist/$playlistId"
    }
    data object UserProfile : Screen("user_profile")

    companion object {
        val bottomNavItems = listOf(Home, Search, Library, Premium, Create)
    }
}
