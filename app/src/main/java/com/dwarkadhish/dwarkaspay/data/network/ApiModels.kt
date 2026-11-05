package com.dwarkadhish.dwarkaspay.data.network

import com.google.gson.annotations.SerializedName

// ── Request models ───────────────────────────────────────────────────────────

data class RegisterRequest(
    @SerializedName("name")            val name: String,
    @SerializedName("email")           val email: String,
    @SerializedName("password")        val password: String,
    @SerializedName("profilePhotoUri") val profilePhotoUri: String? = null,
    @SerializedName("birthdate")       val birthdate: String? = null,
    @SerializedName("age")             val age: Int? = null,
    @SerializedName("gender")          val gender: String? = null
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp")   val otp: String
)

data class ResendOtpRequest(
    @SerializedName("email") val email: String
)

data class UpdateProfileRequest(
    @SerializedName("profilePhotoUri") val profilePhotoUri: String?,
    @SerializedName("birthdate")       val birthdate: String?,
    @SerializedName("age")             val age: Int?,
    @SerializedName("gender")          val gender: String?
)

// ── Response models ──────────────────────────────────────────────────────────

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code")    val code: String?    // e.g. "EMAIL_NOT_VERIFIED"
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code")    val code: String?,
    @SerializedName("token")   val token: String?,
    @SerializedName("user")    val user: NetworkUser?
)

data class NetworkUser(
    @SerializedName("uid")              val uid: String,
    @SerializedName("name")             val name: String,
    @SerializedName("email")            val email: String,
    @SerializedName("isEmailVerified")  val isEmailVerified: Boolean,
    @SerializedName("profilePhotoUri")  val profilePhotoUri: String?,
    @SerializedName("birthdate")        val birthdate: String?,
    @SerializedName("age")              val age: Int?,
    @SerializedName("gender")           val gender: String?,
    @SerializedName("providerId")       val providerId: String?
)
