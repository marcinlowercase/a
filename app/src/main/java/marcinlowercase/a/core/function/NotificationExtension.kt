package marcinlowercase.a.core.function

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(context: Context) {
    val name = "downloads"
    val descriptionText = "shows download progress and completion"
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel("download_channel", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}