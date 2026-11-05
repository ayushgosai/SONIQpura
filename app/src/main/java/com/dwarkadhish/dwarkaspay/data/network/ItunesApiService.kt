package com.dwarkadhish.dwarkaspay.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ── iTunes Search API Models ──────────────────────────────────────────────────

data class ItunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results")     val results: List<ItunesTrack>
)

data class ItunesTrack(
    @SerializedName("trackId")         val trackId: Long?,
    @SerializedName("trackName")       val trackName: String?,
    @SerializedName("artistName")      val artistName: String?,
    @SerializedName("collectionName")  val collectionName: String?,
    @SerializedName("artworkUrl100")   val artworkUrl100: String?,
    @SerializedName("artworkUrl60")    val artworkUrl60: String?,
    @SerializedName("previewUrl")      val previewUrl: String?,   // 30-sec preview MP3
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("primaryGenreName")val primaryGenreName: String?,
    @SerializedName("trackPrice")      val trackPrice: Double?,
    @SerializedName("releaseDate")     val releaseDate: String?,
    @SerializedName("kind")            val kind: String?
)

// ── Retrofit interface ────────────────────────────────────────────────────────

interface ItunesApiService {
    @GET("search")
    suspend fun search(
        @Query("term")      term: String,
        @Query("media")     media: String = "music",
        @Query("entity")    entity: String = "song",
        @Query("limit")     limit: Int = 30,
        @Query("country")   country: String = "IN"
    ): ItunesSearchResponse
}

// ── Singleton client ──────────────────────────────────────────────────────────

object ItunesClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: ItunesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApiService::class.java)
    }
}
