package com.dwarkadhish.dwarkaspay.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.dwarkadhish.dwarkaspay.model.EqualizerSettings
import com.dwarkadhish.dwarkaspay.model.PlaybackState
import com.dwarkadhish.dwarkaspay.model.RepeatMode
import com.dwarkadhish.dwarkaspay.model.SleepTimerOption
import com.dwarkadhish.dwarkaspay.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class AudioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    val exoPlayer: ExoPlayer by lazy {
        // Apple's preview CDN + other streaming hosts reject some default clients,
        // so use a normal browser-like User-Agent and allow HTTPS redirects.
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) VioraMusic/1.0")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)
        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultDataSource.Factory(context, httpDataSourceFactory)
        )

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // Handle audio focus automatically
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL) // Keep CPU awake while music is playing
            .build().apply {
                addListener(PlayerListener())
            }
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _sleepTimerOption = MutableStateFlow(SleepTimerOption.OFF)
    val sleepTimerOption: StateFlow<SleepTimerOption> = _sleepTimerOption.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow<Long?>(null)
    val sleepTimerSecondsLeft: StateFlow<Long?> = _sleepTimerSecondsLeft.asStateFlow()

    private var sleepTimerJob: Job? = null

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private var currentQueueTracks: List<Track> = emptyList()

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            val isLoading = state == Player.STATE_BUFFERING
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            _playbackState.update {
                it.copy(
                    isLoading = isLoading,
                    durationMs = if (duration > 0) duration else it.durationMs
                )
            }
            if (state == Player.STATE_ENDED) {
                if (_sleepTimerOption.value == SleepTimerOption.END_OF_TRACK) {
                    pause()
                    _sleepTimerOption.value = SleepTimerOption.OFF
                } else {
                    skipToNext()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = exoPlayer.currentMediaItemIndex
            if (index in currentQueueTracks.indices) {
                val track = currentQueueTracks[index]
                _playbackState.update {
                    it.copy(
                        currentTrack = track,
                        currentQueueIndex = index,
                        durationMs = track.durationMs,
                        playbackPositionMs = 0L
                    )
                }
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            android.util.Log.e("AudioPlayerManager", "ExoPlayer error: ${error.errorCodeName}", error)
            _playbackState.update {
                it.copy(
                    isPlaying = false,
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Playback error"
                )
            }
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = listOf(track)) {
        val targetQueue = if (newQueue.contains(track)) newQueue else listOf(track) + newQueue
        val startIndex = targetQueue.indexOf(track).coerceAtLeast(0)
        setQueueAndPlay(targetQueue, startIndex)
    }

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        currentQueueTracks = tracks

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(Uri.parse(track.mediaUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        ensurePlaybackServiceStarted()
        exoPlayer.play()

        val activeTrack = tracks.getOrNull(startIndex)
        _playbackState.update {
            it.copy(
                currentTrack = activeTrack,
                queue = tracks,
                currentQueueIndex = startIndex,
                isPlaying = true,
                durationMs = activeTrack?.durationMs ?: 0L,
                playbackPositionMs = 0L
            )
        }
    }

    private fun ensurePlaybackServiceStarted() {
        try {
            val intent = Intent(context, VioraPlaybackService::class.java)
            // Always use startService — MediaSessionService promotes itself to foreground
            // automatically via its internal notification provider once ExoPlayer starts playing.
            // Using startForegroundService() here causes a ForegroundServiceDidNotStartInTimeException
            // crash on API 26+ when the OS watchdog timer (5s) fires before playback begins.
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            ensurePlaybackServiceStarted()
            if (exoPlayer.playbackState == Player.STATE_IDLE && currentQueueTracks.isNotEmpty()) {
                val index = _playbackState.value.currentQueueIndex.coerceAtLeast(0)
                setQueueAndPlay(currentQueueTracks, index)
            } else {
                exoPlayer.play()
            }
        }
    }

    fun resume() {
        if (!exoPlayer.isPlaying) {
            ensurePlaybackServiceStarted()
            exoPlayer.play()
        }
    }

    fun pause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    fun seekToProgress(fraction: Float) {
        val duration = if (exoPlayer.duration > 0) exoPlayer.duration else _playbackState.value.durationMs
        if (duration > 0) {
            val position = (duration * fraction.coerceIn(0f, 1f)).toLong()
            seekTo(position)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackState.update { it.copy(playbackPositionMs = positionMs) }
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (currentQueueTracks.isNotEmpty()) {
            exoPlayer.seekToDefaultPosition(0) // Wrap around to first track
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L) // Rewind if played more than 3s
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else if (currentQueueTracks.isNotEmpty()) {
            exoPlayer.seekToDefaultPosition(currentQueueTracks.lastIndex) // Wrap to last track
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffleEnabled
        exoPlayer.shuffleModeEnabled = newShuffle
        _playbackState.update { it.copy(isShuffleEnabled = newShuffle) }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        exoPlayer.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _playbackState.update {
                        it.copy(
                            playbackPositionMs = pos,
                            durationMs = if (dur > 0) dur else it.durationMs
                        )
                    }
                }
                delay(300L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimerJob?.cancel()
        _sleepTimerOption.value = option
        when {
            option == SleepTimerOption.OFF -> {
                _sleepTimerSecondsLeft.value = null
            }
            option == SleepTimerOption.END_OF_TRACK -> {
                _sleepTimerSecondsLeft.value = null
            }
            option.minutes > 0 -> {
                val totalSec = option.minutes * 60L
                _sleepTimerSecondsLeft.value = totalSec
                sleepTimerJob = scope.launch {
                    var remaining = totalSec
                    while (remaining > 0 && isActive) {
                        delay(1000L)
                        remaining -= 1
                        _sleepTimerSecondsLeft.value = remaining
                    }
                    if (isActive) {
                        pause()
                        _sleepTimerOption.value = SleepTimerOption.OFF
                        _sleepTimerSecondsLeft.value = null
                    }
                }
            }
        }
    }

    fun updateEqualizer(settings: EqualizerSettings) {
        _equalizerSettings.value = settings
    }

    fun release() {
        stopProgressTracker()
        sleepTimerJob?.cancel()
        exoPlayer.release()
    }
}
