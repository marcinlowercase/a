package marcinlowercase.a.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.MainActivity
import marcinlowercase.a.R

private const val ACTION_PLAY = "marcinlowercase.a.PLAY"
private const val ACTION_PAUSE = "marcinlowercase.a.PAUSE"
private const val ACTION_PREV_TRACK = "marcinlowercase.a.PREV_TRACK"
private const val ACTION_NEXT_TRACK = "marcinlowercase.a.NEXT_TRACK"
private const val ACTION_PREV_5 = "marcinlowercase.a.PREV_5"
private const val ACTION_NEXT_5 = "marcinlowercase.a.NEXT_5"

class MediaPlaybackService : Service() {
    private val channelId = "media_playback_channel"
    private val notificationId = 101
    private var mediaSession: MediaSessionCompat? = null

    private var currentTitle = "Browser Video"
    private var currentArtist = ""
    private var currentPositionMs = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
    private var currentDurationMs = -1L
    private var currentSpeed = 1.0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FROM_MEDIA_CONTROL", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSessionCompat(this, "BrowserMedia").apply {
            isActive = true
            setSessionActivity(pendingIntent)

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { handleCommand("play") }
                override fun onPause() { handleCommand("pause") }
                override fun onSkipToPrevious() { handleCommand("prev_track") }
                override fun onSkipToNext() { handleCommand("next_track") }
                override fun onRewind() { handleCommand("prev_5") }
                override fun onFastForward() { handleCommand("next_5") }
                override fun onSeekTo(pos: Long) {
                    (applicationContext as? CustomApplication)?.geckoManager?.sendVideoCommand("seek_to", pos / 1000.0)
                }
                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        ACTION_PREV_5 -> handleCommand("prev_5")
                        ACTION_NEXT_5 -> handleCommand("next_5")
                    }
                }
            })
        }
    }

    private fun handleCommand(command: String) {
        (applicationContext as? CustomApplication)?.geckoManager?.sendVideoCommand(command)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId, "Media Controls", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows what is playing in the browser"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handleCommand("play")
            ACTION_PAUSE -> handleCommand("pause")
            ACTION_PREV_TRACK -> handleCommand("prev_track")
            ACTION_NEXT_TRACK -> handleCommand("next_track")
            ACTION_PREV_5 -> handleCommand("prev_5")
            ACTION_NEXT_5 -> handleCommand("next_5")
        }

        intent?.getStringExtra("TITLE")?.let { currentTitle = it }
        intent?.getStringExtra("ARTIST")?.let { currentArtist = it }
        if (intent?.hasExtra("POSITION_MS") == true) {
            currentPositionMs = intent.getLongExtra("POSITION_MS", PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN)
        }
        if (intent?.hasExtra("DURATION_MS") == true) {
            currentDurationMs = intent.getLongExtra("DURATION_MS", -1L)
        }
        if (intent?.hasExtra("SPEED") == true) {
            currentSpeed = intent.getFloatExtra("SPEED", 1.0f)
        }
        val isPaused = intent?.getBooleanExtra("IS_PAUSED", false) ?: false

        updateNotification(currentTitle, currentArtist, isPaused)
        return START_STICKY
    }

    private fun serviceActionPendingIntent(requestCode: Int, action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MediaPlaybackService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification(title: String, artist: String, isPaused: Boolean) {
        val state = if (isPaused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, currentPositionMs, if (isPaused) 0f else currentSpeed)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    ACTION_PREV_5,
                    "Back 5s",
                    R.drawable.ic_replay_5
                ).build()
            )
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    ACTION_NEXT_5,
                    "Forward 5s",
                    R.drawable.ic_forward_5
                ).build()
            )
            .build()
        mediaSession?.setPlaybackState(playbackState)

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        if (currentDurationMs > 0L) {
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentDurationMs)
        }
        mediaSession?.setMetadata(metadataBuilder.build())

        val prevTrackAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous,
            "Previous",
            serviceActionPendingIntent(3, ACTION_PREV_TRACK)
        )
        val prev5Action = NotificationCompat.Action(
            R.drawable.ic_replay_5,
            "Back 5s",
            serviceActionPendingIntent(4, ACTION_PREV_5)
        )
        val toggleAction = if (isPaused) {
            NotificationCompat.Action(
                R.drawable.ic_play_arrow,
                "Play",
                serviceActionPendingIntent(1, ACTION_PLAY)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pause",
                serviceActionPendingIntent(2, ACTION_PAUSE)
            )
        }
        val next5Action = NotificationCompat.Action(
            R.drawable.ic_forward_5,
            "Forward 5s",
            serviceActionPendingIntent(5, ACTION_NEXT_5)
        )
        val nextTrackAction = NotificationCompat.Action(
            R.drawable.ic_skip_next,
            "Next",
            serviceActionPendingIntent(6, ACTION_NEXT_TRACK)
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_video_camera_back)
            .setContentTitle(title)
            .setContentText(artist)
            .setOngoing(!isPaused)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mediaSession?.controller?.sessionActivity)
            .addAction(prevTrackAction)
            .addAction(prev5Action)
            .addAction(toggleAction)
            .addAction(next5Action)
            .addAction(nextTrackAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 2, 4)
            )

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, notification)
        }
    }

    override fun onDestroy() {
        mediaSession?.isActive = false
        mediaSession?.release()
        super.onDestroy()
    }
}