package me.linhvo.ittakestwo.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.linhvo.ittakestwo.MainActivity
import me.linhvo.ittakestwo.R

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        Log.d("debug_firebase", "FID: $installationId")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
        val content = message.notification?.body

        if (title != null && content != null) {
            showNotification(title, content)
        }

        Log.d(
            "debug_firebase",
            "message received:\n title: ${message.notification?.title}\n body: ${message.notification?.body}"
        )

    }

    fun showNotification(senderName: String, content: String) {
        val channelId = "new_chat_message"
        val channelName = "New Message"

        val notificationManager: NotificationManager =
            this.getSystemService(NotificationManager::class.java) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory("Chat")

        if (notificationManager.getNotificationChannel(channelId) == null) {
            notificationManager.createNotificationChannelGroup(NotificationChannelGroup("Chat", "Chat"))
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                group = "Chat"
            }
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(this)) {
            if (notificationManager.areNotificationsEnabled()) {
                notify(1, builder.build())
                Log.d("debug_noti", "show noti")
            }
        }
    }
}