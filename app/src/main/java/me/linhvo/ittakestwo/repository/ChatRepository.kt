package me.linhvo.ittakestwo.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.MessageDao
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.network.datasource.MessageNetworkDataSource
import me.linhvo.ittakestwo.network.model.NetworkMessage
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val messageNetworkDataSource: MessageNetworkDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    suspend fun getMessages(): List<Message> = messageDao.loadMessagesOrderByLatest()

    fun getMessageListStream(): Flow<List<Message>> = messageDao.observeMessagesOrderByLatest()

    fun addNewMessage(senderId: String, recipientId: String, content: String) {
        applicationScope.launch {
            try {
                val newMessage = NetworkMessage(
                    senderId = senderId,
                    recipientId = recipientId,
                    content = content,
                )

                messageNetworkDataSource.addMessage(newMessage).let {
                    val message = it.toDomainModel(senderId)
                    message.isSenderMe = true
                    messageDao.upsert(message)
                }
            } catch (e: Exception) {
                Log.d("debug_addMessage_error", e.toString())
            }
        }
    }

    suspend fun getUnreadCount(userId: String): Int = messageDao.getUnreadMessages(userId).size

    suspend fun markAllAsRead(userId: String) {
        messageDao.getUnreadMessages(userId).forEach {
            it.readAt = Clock.System.now()
            messageDao.upsert(it)
            messageNetworkDataSource.updateMessage(messageId = it.id, timestamp = it.readAt)
        }
    }

    suspend fun populateMessagesToLocalDatabase(userId: String) {
        messageNetworkDataSource.getMessages(userId).forEach {
            messageDao.upsert(it.toDomainModel(userId))
        }
        Log.d("debug_messages", "populated messages")
    }

    suspend fun syncMessages(currentUser: String) {
        Log.d("debug_messages", "syncing messages")
        messageNetworkDataSource.getMessageStream(currentUser).collect {
            messageDao.upsert(it.toDomainModel(currentUser))
        }
    }
}