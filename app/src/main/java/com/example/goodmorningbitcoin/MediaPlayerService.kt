package com.example.goodmorningbitcoin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log

class MediaPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var mediaSession: MediaSessionCompat
    private val notificationId = 1
    private val channelId = "media_playback_channel"

    companion object {
        const val ACTION_PLAY = "com.example.goodmorningbitcoin.ACTION_PLAY"
        const val ACTION_STOP = "com.example.goodmorningbitcoin.ACTION_STOP"
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service does not support binding, so return null
        return null
    }

    override fun onCreate() {
        super.onCreate()

// Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "MediaSessionTag").apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }


        // Create a notification channel (required for Android Oreo and above)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Media Playback"
            val descriptionText = "Media playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> startPlaying()
            ACTION_STOP -> stopPlaying()
        }
        return START_NOT_STICKY
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            startPlaying()
        }

        override fun onPause() {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource("https://radio.goodmorningbitcoin.com/listen/goodmorningbitcoin/radio.mp3")
                // Set the OnErrorListener to handle errors during playback
                setOnErrorListener { _, what, extra ->
                    Log.e("MediaPlayerError", "Error occurred during playback, Error code: $what, Extra code: $extra")
                    // Handle the error as appropriate for your app
                    true
                }
                // Set the OnPreparedListener to start playback once prepared
                setOnPreparedListener {
                    start()
                }
                // Use prepareAsync to prepare the media player asynchronously
                prepareAsync()
            }
            isPlaying = true
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            // Show media player notification
            showMediaPlayerNotification(PlaybackStateCompat.STATE_PLAYING)
            // Start service as a foreground service
            startForeground(notificationId, buildNotification(PlaybackStateCompat.STATE_PLAYING))
        } catch (e: Exception) {
            Log.e("MediaPlayerError", "Error occurred while starting playback", e)
            // Handle the exception as appropriate for your app
        }
    }

    private fun stopPlaying() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        // Update media player notification
        showMediaPlayerNotification(PlaybackStateCompat.STATE_PAUSED)
        // Stop service as a foreground service
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun updatePlaybackState(state: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    private fun showMediaPlayerNotification(state: Int) {
        val notification = buildNotification(state)
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }

    private fun buildNotification(state: Int): Notification {
        val playIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = ACTION_PLAY
        }
        val pauseIntent = Intent(this, MediaPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val playPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system info icon
            .setContentTitle("Good Morning Bitcoin")
            .setContentText("Pending Pulling Podcast Name")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            notificationBuilder
                .addAction(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    pausePendingIntent
                ) // Use system pause icon
        } else {
            notificationBuilder
                .addAction(
                    android.R.drawable.ic_media_play,
                    "Play",
                    playPendingIntent
                ) // Use system play icon
        }

        return notificationBuilder.build()
    }

}