package com.dwarkadhish.dwarkaspay.data.auth

import com.dwarkadhish.dwarkaspay.model.AuthUser

/**
 * Where the signed-in account came from.
 *
 * The provider is stored so the rest of the app can tell an email/password account
 * (which always needs the custom 6-digit OTP) apart from a Google account whose
 * email Firebase already considers verified.
 */
enum class AuthProvider {
    EMAIL_PASSWORD,
    GOOGLE
}

typealias LegacyAuthUser = com.dwarkadhish.dwarkaspay.model.AuthUser

/**
 * Single source of truth for splash/session routing.
 * The UI must route from this state, never from a locally stored boolean.
 */
sealed interface AuthSessionState {

    /** Splash is still waiting for the first auth state callback. */
    object Checking : AuthSessionState

    /** Nobody is signed in (this is also what the placeholder repository reports). */
    object SignedOut : AuthSessionState

    /** Email/password account signed in but the 6-digit OTP is still pending. */
    data class PendingVerification(val email: String) : AuthSessionState

    /** Verified account - safe to open protected screens such as Home. */
    data class Authenticated(val user: AuthUser) : AuthSessionState
}

/** Result wrapper: the UI never throws, it always inspects success/failure. */
sealed class AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>()
    data class Failure(val error: AuthError) : AuthResult<Nothing>()
}

/**
 * Everything the user may see after a failed auth call.
 * Messages are already human readable so composables never map error codes.
 */
sealed class AuthError(
    val message: String,
    val code: String,
    val isRetryable: Boolean = false
) {

    /** Firebase/Cloud Functions phase not wired up yet - never fakes success. */
    object ServiceNotConfigured : AuthError(
        message = "Authentication service is not connected yet. Finish the Firebase setup first.",
        code = "service-not-configured"
    )

    object Network : AuthError(
        message = "No internet connection. Please try again.",
        code = "network",
        isRetryable = true
    )

    object InvalidCredentials : AuthError(
        message = "Email or password is incorrect.",
        code = "invalid-credentials"
    )

    object InvalidEmail : AuthError(
        message = "That email address doesn't look right.",
        code = "invalid-email"
    )

    object EmailAlreadyInUse : AuthError(
        message = "An account with this email already exists. Try logging in instead.",
        code = "email-already-in-use"
    )

    object WeakPassword : AuthError(
        message = "Choose a stronger password (8+ characters with letters and numbers).",
        code = "weak-password"
    )

    object EmailNotVerified : AuthError(
        message = "Verify your email address before signing in.",
        code = "email-not-verified"
    )

    object OtpInvalid : AuthError(
        message = "That code isn't correct. Check your inbox and try again.",
        code = "otp-invalid"
    )

    object OtpExpired : AuthError(
        message = "That code has expired. Request a new one.",
        code = "otp-expired"
    )

    object TooManyAttempts : AuthError(
        message = "Too many attempts. Wait a few minutes before trying again.",
        code = "too-many-attempts"
    )

    object ResendLimited : AuthError(
        message = "You have requested too many codes. Try again in a few minutes.",
        code = "resend-limited"
    )

    object OperationCancelled : AuthError(
        message = "Sign-in was cancelled.",
        code = "cancelled"
    )

    object Unknown : AuthError(
        message = "Something went wrong. Please try again.",
        code = "unknown",
        isRetryable = true
    )
}

/**
 * Metadata returned whenever an OTP is issued or re-issued.
 * Expiry, resend cooldown and attempt budget are decided server side;
 * the client only renders them.
 */
data class OtpDelivery(
    val expiresInSeconds: Int,
    val resendInSeconds: Int,
    val attemptsAllowed: Int
)
