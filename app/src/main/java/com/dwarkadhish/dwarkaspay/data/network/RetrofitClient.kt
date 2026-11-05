package com.dwarkadhish.dwarkaspay.data.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * The backend runs on the laptop. Its LAN IP changes whenever Windows
     * switches between Wi-Fi and Ethernet, so try every known address instead
     * of hardcoding one. Order = most likely first.
     */
    private val BASE_URLS = listOf(
        "http://10.113.155.164:3000/",   // laptop Wi-Fi
        "http://10.84.143.201:3000/"     // laptop Ethernet / hotspot
    )

    @Volatile
    private var activeBaseUrl: String? = null

    @Volatile
    private var overrideBaseUrl: String? = null

    /** Lets the user point the app at the laptop IP without rebuilding the APK. */
    fun setOverrideBaseUrl(url: String?) {
        overrideBaseUrl = url?.trim()?.takeIf { it.isNotBlank() }?.let {
            val withScheme = if (it.startsWith("http")) it else "http://$it"
            if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        }
        activeBaseUrl = overrideBaseUrl
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val byBaseUrl = mutableMapOf<String, Retrofit>()

    private fun retrofitFor(baseUrl: String): Retrofit = synchronized(byBaseUrl) {
        byBaseUrl.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    val apiService: VioraApiService by lazy {
        retrofitFor(activeBaseUrl ?: BASE_URLS.first()).create(VioraApiService::class.java)
    }

    /**
     * Tries each backend address until one answers, then remembers it so the
     * next call goes straight to the right host.
     */
    private suspend fun <T> callBackend(block: suspend (Retrofit) -> T): T {
        val candidates = (listOfNotNull(activeBaseUrl, overrideBaseUrl) + BASE_URLS).distinct()
        var lastError: Exception? = null

        for (baseUrl in candidates) {
            try {
                val result = withTimeout(8_000) {
                    withContext(Dispatchers.IO) { block(retrofitFor(baseUrl)) }
                }
                activeBaseUrl = baseUrl
                return result
            } catch (e: TimeoutCancellationException) {
                lastError = e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("Viora backend unreachable")
    }

    suspend fun saavnSearch(query: String, limit: Int = 30): SaavnSearchResponse =
        callBackend { retrofit -> retrofit.create(SaavnApiService::class.java).search(query, limit) }

    suspend fun spotifySearch(query: String, limit: Int = 30): SpotifySearchResponse =
        callBackend { retrofit -> retrofit.create(SpotifyApiService::class.java).search(query, limit) }
}
