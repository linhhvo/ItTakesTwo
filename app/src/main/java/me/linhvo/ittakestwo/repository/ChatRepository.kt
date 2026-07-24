package me.linhvo.ittakestwo.repository

import kotlinx.coroutines.flow.Flow
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
    private val messageNetworkDataSource: MessageNetworkDataSource
) {

    suspend fun getMessages(): List<Message> = messageDao.loadMessagesOrderByLatest()

    fun getMessageListStream(): Flow<List<Message>> = messageDao.observeMessagesOrderByLatest()

    suspend fun addNewMessage(senderId: String, recipientId: String, content: String) {
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
    }

    suspend fun syncMessages(currentUser: String) {
        messageNetworkDataSource.getMessageStream(currentUser).collect {
            messageDao.upsert(it.toDomainModel(currentUser))
        }
    }
}