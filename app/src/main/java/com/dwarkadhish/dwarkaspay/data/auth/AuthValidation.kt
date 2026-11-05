package com.dwarkadhish.dwarkaspay.data.auth

/**
 * Password strength buckets used by the Create Account strength meter.
 * [score] drives the number of filled segments (0..4).
 */
enum class PasswordStrength(val label: String, val score: Int) {
    EMPTY("Enter a password", 0),
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    GOOD("Good", 3),
    STRONG("Strong", 4)
}

/**
 * Pure, UI-free validation so both ViewModels and unit tests share one policy.
 * No password or OTP ever gets logged from here.
 */
object AuthValidation {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private const val MAX_EMAIL_LENGTH = 254

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.length <= MAX_EMAIL_LENGTH && EMAIL_REGEX.matches(trimmed)
    }

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Enter your full name"
        name.trim().length < 2 -> "That name looks too short"
        name.trim().length > 60 -> "That name is too long"
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Enter your email address"
        !isValidEmail(email) -> "Enter a valid email address"
        else -> null
    }

    /** Login only checks presence - the policy belongs to registration. */
    fun loginPasswordError(password: String): String? = when {
        password.isEmpty() -> "Enter your password"
        else -> null
    }

    fun registrationPasswordError(password: String): String? = when {
        password.isEmpty() -> "Create a password"
        password.length < 8 -> "Use at least 8 characters"
        !password.any { it.isLetter() } -> "Add at least one letter"
        !password.any { it.isDigit() } -> "Add at least one number"
        else -> null
    }

    fun confirmPasswordError(password: String, confirmation: String): String? = when {
        confirmation.isEmpty() -> "Re-enter your password"
        confirmation != password -> "Passwords don't match"
        else -> null
    }

    fun strength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return when {
            score <= 1 -> PasswordStrength.WEAK
            score == 2 -> PasswordStrength.FAIR
            score <= 4 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }
}
