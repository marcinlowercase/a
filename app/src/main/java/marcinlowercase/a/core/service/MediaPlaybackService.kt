package marcinlowercase.a.core.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.MainActivity

private const val ACTION_PLAY = "marcinlowercase.a.PLAY"
private const val ACTION_PAUSE = "marcinlowercase.a.PAUSE"

class MediaPlaybackService : Service() {
    private val channelId = "media_playback_channel"
    private val notificationId = 101
    private var mediaSession: MediaSessionCompat? = null

    private var currentTitle = "Browser Video"

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
                override fun onPlay() { handlePlay() }
                override fun onPause() { handlePause() }
            })
        }
    }

    private fun handlePlay() {
        (applicationContext as? CustomApplication)?.geckoManager?.activeGeckoMediaSession?.play()
    }

    private fun handlePause() {
        (applicationContext as? CustomApplication)?.geckoManager?.activeGeckoMediaSession?.pause()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Media Controls", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows what is playing in the browser"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
        }

        intent?.getStringExtra("TITLE")?.let { currentTitle = it }
        val isPaused = intent?.getBooleanExtra("IS_PAUSED", false) ?: false

        updateNotification(currentTitle, isPaused)
        return START_STICKY
    }

    private fun updateNotification(title: String, isPaused: Boolean) {
        val state = if (isPaused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
            .build()
        mediaSession?.setPlaybackState(playbackState)

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .build()
        mediaSession?.setMetadata(metadata)

        val playIntent = PendingIntent.getService(this, 1,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PLAY },
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(this, 2,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_IMMUTABLE
        )

        val toggleAction = if (isPaused) {
            NotificationCompat.Action(marcinlowercase.a.R.drawable.ic_play_arrow, "Play", playIntent)
        } else {
            NotificationCompat.Action(marcinlowercase.a.R.drawable.ic_pause, "Pause", pauseIntent)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(marcinlowercase.a.R.drawable.ic_video_camera_back)
            .setContentTitle(title) // Only the Title is shown now
            .setOngoing(!isPaused)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mediaSession?.controller?.sessionActivity)
            .addAction(toggleAction)
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession?.sessionToken)
                .setShowActionsInCompactView(0)
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