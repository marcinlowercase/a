//package marcinlowercase.a.core.service
//
//import android.app.*
//import android.content.Intent
//import android.content.pm.ServiceInfo
//import android.os.Build
//import android.os.IBinder
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.media.app.NotificationCompat.MediaStyle
//import marcinlowercase.a.CustomApplication
//import marcinlowercase.a.MainActivity
//
//class MediaPlaybackService : Service() {
//    private val CHANNEL_ID = "media_playback_channel"
//    private val NOTIFICATION_ID = 101
//    private var mediaSession: MediaSessionCompat? = null
//
//    private var currentTitle = "Browser Video"
//    private var currentArtist = "Website"
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannel()
//
//        // 1. Create the Intent that opens your app when the card is tapped
//        val activityIntent = Intent(this, MainActivity::class.java).apply {
//            // SINGLE_TOP ensures we don't open multiple copies of the app
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("FROM_MEDIA_CONTROL", true)
//        }
//
//        // 2. Wrap it in a PendingIntent (Required for security on Android 12+)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            activityIntent,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
//        )
//
//        // 3. Initialize MediaSessionCompat
//        mediaSession = MediaSessionCompat(this, "BrowserMedia").apply {
//            isActive = true
//
//            // LINK: This makes the background tap on the Android Media Card work
//            setSessionActivity(pendingIntent)
//
//            setCallback(object : MediaSessionCompat.Callback() {
//                override fun onPlay() {
//                    (applicationContext as? CustomApplication)?.geckoManager
//                        ?.activeGeckoMediaSession?.play()
//                }
//
//                override fun onPause() {
//                    (applicationContext as? CustomApplication)?.geckoManager
//                        ?.activeGeckoMediaSession?.pause()
//                }
//            })
//        }
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID, "Media Controls", NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Shows what is playing in the browser"
//                setShowBadge(false)
//            }
//            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        intent?.getStringExtra("TITLE")?.let { currentTitle = it }
//        intent?.getStringExtra("ARTIST")?.let { currentArtist = it }
//
//        val isPaused = intent?.getBooleanExtra("IS_PAUSED", false) ?: false
//
//        updateNotification(currentTitle, currentArtist, isPaused)
//        return START_STICKY
//    }
//
//    private fun updateNotification(title: String, artist: String, isPaused: Boolean) {
//        // 1. Update Playback State
//        val state = if (isPaused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING
//        val playbackState = PlaybackStateCompat.Builder()
//            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
//            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
//            .build()
//        mediaSession?.setPlaybackState(playbackState)
//
//        // 2. Update Metadata
//        val metadata = MediaMetadataCompat.Builder()
//            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
//            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
//            .build()
//        mediaSession?.setMetadata(metadata)
//
//        // 3. Build the Notification
//        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(marcinlowercase.a.R.drawable.ic_video_camera_back)
//            .setContentTitle(title)
//            .setContentText(artist)
//            .setOngoing(!isPaused)
//            .setSilent(true)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            // This is the fallback tap handler
//            .setContentIntent(mediaSession?.controller?.sessionActivity)
//            .setStyle(MediaStyle()
//                .setMediaSession(mediaSession?.sessionToken)
//                .setShowActionsInCompactView(0)
//            )
//
//        val notification = notificationBuilder.build()
//
//        // 4. Start/Update Foreground Service
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        } else {
//            startForeground(NOTIFICATION_ID, notification)
//        }
//    }
//
//    override fun onDestroy() {
//        mediaSession?.isActive = false
//        mediaSession?.release()
//        super.onDestroy()
//    }
//}

package marcinlowercase.a.core.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.MainActivity

// Action constants for the buttons
private const val ACTION_PLAY = "marcinlowercase.a.PLAY"
private const val ACTION_PAUSE = "marcinlowercase.a.PAUSE"

class MediaPlaybackService : Service() {
    private val CHANNEL_ID = "media_playback_channel"
    private val NOTIFICATION_ID = 101
    private var mediaSession: MediaSessionCompat? = null

    private var currentTitle = "Browser Video"
    private var currentArtist = "Website"

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
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

    // Helper functions to talk to Gecko
    private fun handlePlay() {
        (applicationContext as? CustomApplication)?.geckoManager?.activeGeckoMediaSession?.play()
    }

    private fun handlePause() {
        (applicationContext as? CustomApplication)?.geckoManager?.activeGeckoMediaSession?.pause()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Media Controls", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows what is playing in the browser"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Stop the foreground service immediately when the user swipes the app away
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle button clicks from the notification actions
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
        }

        intent?.getStringExtra("TITLE")?.let { currentTitle = it }
        intent?.getStringExtra("ARTIST")?.let { currentArtist = it }
        val isPaused = intent?.getBooleanExtra("IS_PAUSED", false) ?: false

        updateNotification(currentTitle, currentArtist, isPaused)
        return START_STICKY
    }

    private fun updateNotification(title: String, artist: String, isPaused: Boolean) {
        val state = if (isPaused) PlaybackStateCompat.STATE_PAUSED else PlaybackStateCompat.STATE_PLAYING
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
            .build()
        mediaSession?.setPlaybackState(playbackState)

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .build()
        mediaSession?.setMetadata(metadata)

        // --- NEW: Create PendingIntents for the buttons ---
        val playIntent = PendingIntent.getService(this, 1,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PLAY },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val pauseIntent = PendingIntent.getService(this, 2,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PAUSE },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        // Choose which icon to show as the main action
        val toggleAction = if (isPaused) {
            NotificationCompat.Action(marcinlowercase.a.R.drawable.ic_play_arrow, "Play", playIntent)
        } else {
            NotificationCompat.Action(marcinlowercase.a.R.drawable.ic_pause, "Pause", pauseIntent)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(marcinlowercase.a.R.drawable.ic_video_camera_back)
            .setContentTitle(title)
            .setContentText(artist)
            .setOngoing(!isPaused)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mediaSession?.controller?.sessionActivity)
            // ADD THE ACTION HERE (Index 0)
            .addAction(toggleAction)
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession?.sessionToken)
                // This now refers to the 'toggleAction' we just added
                .setShowActionsInCompactView(0)
            )

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        mediaSession?.isActive = false
        mediaSession?.release()
        super.onDestroy()
    }
}