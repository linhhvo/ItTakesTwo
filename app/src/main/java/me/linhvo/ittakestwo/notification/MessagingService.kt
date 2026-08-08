package me.linhvo.ittakestwo.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.MainActivity
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.repository.AuthRepository
import me.linhvo.ittakestwo.repository.ChatRepository
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        scope.launch {
            chatRepository.populateMessagesToLocalDatabase(authRepository.currentUserId)
        }

        val title = message.notification?.title
        val content = message.notification?.body

//        if (AppNavigation.currentRoute != Route.Chat) {
//        if (title != null && content != null) {
//            showNotification(title, content)
//            }
//        }
    }

    fun showNotification(senderName: String, content: String, screen: String? = null) {
        val channelId = "new_chat_message"
        val channelName = "New Message"

        val notificationManager: NotificationManager =
            this.getSystemService(NotificationManager::class.java) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = "https://ittakestwo.linhvo.me/$screen".toUri()
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
            }
        }
    }
}