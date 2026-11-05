package com.dwarkadhish.dwarkaspay.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionResult
import com.dwarkadhish.dwarkaspay.MainActivity
import com.dwarkadhish.dwarkaspay.R
import com.dwarkadhish.dwarkaspay.VioraApplication
import com.google.common.collect.ImmutableList

@OptIn(UnstableApi::class)
class VioraPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "viora_playback_channel_v3"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val app = application as VioraApplication
        val player = app.audioPlayerManager.exoPlayer

        // Intent for clicking the notification to return to MainActivity
        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build MediaSession with callback that handles next, previous, and declares all commands
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(VioraMediaSessionCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        // Configure default media notification provider with dedicated 2D vector icon (NOT adaptive icon)
        val defaultNotificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.notification_channel_playback)
            .setNotificationId(NOTIFICATION_ID)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_music_notification)
            }

        // Wrap provider to ensure notification has VISIBILITY_PUBLIC for lockscreen display
        val lockscreenNotificationProvider = object : MediaNotification.Provider {
            override fun createNotification(
                session: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val mediaNotification = defaultNotificationProvider.createNotification(
                    session,
                    customLayout,
                    actionFactory,
                    onNotificationChangedCallback
                )
                // Force VISIBILITY_PUBLIC so lockscreen on HyperOS/MIUI/OneUI/AOSP shows full controls
                mediaNotification.notification.visibility = Notification.VISIBILITY_PUBLIC
                return mediaNotification
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                return defaultNotificationProvider.handleCustomCommand(session, action, extras)
            }
        }

        setMediaNotificationProvider(lockscreenNotificationProvider)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Delete old lower-importance channel cache so OS updates lockscreen settings
            try {
                notificationManager.deleteNotificationChannel("viora_playback_channel")
                notificationManager.deleteNotificationChannel("viora_playback_channel_v2")
            } catch (_: Exception) {}

            val name = getString(R.string.notification_channel_playback)
            val descriptionText = getString(R.string.notification_channel_playback_desc)

            // IMPORTANCE_DEFAULT is critical: Xiaomi HyperOS / MIUI classifies IMPORTANCE_LOW as "silent"
            // and hides silent notifications from lockscreen by default.
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
                setSound(null, null) // Silent alert sound so song transitions don't chime
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Suppress("DEPRECATION")
    private inner class VioraMediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .build()
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        @Deprecated("Deprecated in Java")
        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            val player = session.player
            when (playerCommand) {
                Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                    } else if (player.mediaItemCount > 0) {
                        player.seekToDefaultPosition(0) // Wrap to first track
                    }
                    return SessionResult.RESULT_SUCCESS
                }
                Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                    if (player.currentPosition > 3000L) {
                        player.seekTo(0L) // Rewind if played more than 3s
                    } else if (player.hasPreviousMediaItem()) {
                        player.seekToPreviousMediaItem()
                    } else if (player.mediaItemCount > 0) {
                        player.seekToDefaultPosition(player.mediaItemCount - 1) // Wrap to last track
                    }
                    return SessionResult.RESULT_SUCCESS
                }
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            super.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            android.util.Log.e("VioraPlaybackService", "Error in onStartCommand: ${e.message}", e)
            START_NOT_STICKY
        }
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        try {
            super.onUpdateNotification(session, startInForegroundRequired)
        } catch (e: Exception) {
            // Guard against BadForegroundServiceNotificationException or ForegroundServiceStartNotAllowedException
            // on custom Android ROMs (MIUI, ColorOS, OneUI, etc.) so playback NEVER crashes
            android.util.Log.e("VioraPlaybackService", "Notification update error guarded: ${e.message}", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
