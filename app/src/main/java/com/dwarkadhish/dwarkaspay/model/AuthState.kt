package com.dwarkadhish.dwarkaspay.model

sealed interface AuthState {
    data object Initializing : AuthState
    data object SignedOut : AuthState
    data class RegistrationPendingVerification(val email: String, val uid: String) : AuthState
    data class VerifyingEmail(val email: String) : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}
