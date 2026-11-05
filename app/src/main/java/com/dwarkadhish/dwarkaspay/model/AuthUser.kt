package com.dwarkadhish.dwarkaspay.model

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
    val providerId: String = "password", // "password" or "google.com"
    val avatarUrl: String? = null,
    // Profile setup fields — filled during registration ProfileSetupScreen
    val profilePhotoUri: String? = null,  // local URI or remote URL
    val birthdate: String? = null,         // "DD/MM/YYYY"
    val age: Int? = null,
    val gender: String? = null             // "Male" / "Female" / "Non-Binary" / "Prefer not to say"
)
