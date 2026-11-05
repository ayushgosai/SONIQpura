package com.dwarkadhish.dwarkaspay.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class SaavnSearchResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("songs") val songs: List<SaavnSongDto> = emptyList()
)

data class SaavnSongDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album") val album: String,
    @SerializedName("image") val image: String,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("streamUrl") val streamUrl: String,
    @SerializedName("permaUrl") val permaUrl: String = "",
    @SerializedName("language") val language: String = ""
)

interface SaavnApiService {
    @GET("api/saavn/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30
    ): SaavnSearchResponse
}
