package com.dwarkadhish.dwarkaspay.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class SpotifySearchResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("tracks") val tracks: List<SpotifyTrackDto> = emptyList()
)

data class SpotifyTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: String,
    @SerializedName("album") val album: String,
    @SerializedName("image") val image: String,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("uri") val uri: String,
    @SerializedName("spotifyUrl") val spotifyUrl: String
)

interface SpotifyApiService {
    @GET("api/spotify/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30
    ): SpotifySearchResponse
}
