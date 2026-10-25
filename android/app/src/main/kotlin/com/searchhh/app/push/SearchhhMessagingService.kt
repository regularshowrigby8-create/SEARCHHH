package com.searchhh.app.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.searchhh.app.MainActivity
import com.searchhh.app.SearchhhApplication
import com.searchhh.app.R
import com.searchhh.app.telemetry.Telemetry
import timber.log.Timber

/**
 * FCM entry point. Only runs when google-services.json is present; without it
 * this service is simply never invoked (Firebase never initialises).
 */
class SearchhhMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Never log the full token in release - it is a capability.
        Timber.i("FCM token refreshed (length=%d)", token.length)
        Telemetry.log("FCM token refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "SEARCHHH"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new alert"
        val deepLinkQuery = message.data["query"]

        Timber.i("Push received: %s", title)
        showNotification(title, body, deepLinkQuery)
    }

    private fun showNotification(title: String, body: String, query: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("POST_NOTIFICATIONS not granted - dropping notification")
            return
        }

        ensureChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!query.isNullOrBlank()) putExtra(EXTRA_QUERY, query)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }.onFailure { Timber.w(it, "Could not post notification") }
    }

    companion object {
        const val CHANNEL_ID = "searchhh_default"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_QUERY = "extra_query"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            manager.createNotificationChannel(channel)
        }
    }
}

/** Small helper so the service does not need a cast at every call site. */
fun Context.searchhhApplication(): SearchhhApplication =
    applicationContext as SearchhhApplication
