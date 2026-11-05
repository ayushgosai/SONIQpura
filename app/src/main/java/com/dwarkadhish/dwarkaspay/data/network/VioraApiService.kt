package com.dwarkadhish.dwarkaspay.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VioraApiService {

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("api/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<ApiResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") bearerToken: String,
        @Body request: UpdateProfileRequest
    ): Response<AuthResponse>

    @GET("api/health")
    suspend fun health(): Response<ApiResponse>
}
