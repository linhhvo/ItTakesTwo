package me.linhvo.ittakestwo.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.AttachmentDao
import me.linhvo.ittakestwo.database.dao.MessageDao
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.network.datasource.MessageNetworkDataSource
import me.linhvo.ittakestwo.network.model.NetworkAttachment
import me.linhvo.ittakestwo.network.model.NetworkMessage
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val messageNetworkDataSource: MessageNetworkDataSource,
    private val attachmentDao: AttachmentDao,
    private val storageRepository: StorageRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val appContext: Context
) {
    private val imageSuffix = appContext.resources.getString(R.string.image_file_suffix)

    fun getMessageListStream(): Flow<List<Message>> = messageDao.observeMessagesOrderByLatest()

    suspend fun getMessageAttachments(messageId: String) =
        attachmentDao.loadAttachments(messageId)

    fun addNewMessage(senderId: String, recipientId: String, content: String, attachments: Set<ByteArray?>) {
        val newMessage = NetworkMessage(
            senderId = senderId,
            recipientId = recipientId,
            content = content,
            attachments = attachments.isNotEmpty()
        )

        appScope.launch {
            val addedMessage = messageNetworkDataSource.addMessage(newMessage).also {
                val message = it.toDomainModel(senderId)
                message.isSenderMe = true
                messageDao.upsertMessage(message)
            }

            if (attachments.isNotEmpty()) {
                Log.d("debug_attachments", attachments.toString())
                attachments.forEachIndexed { index, byteArray ->
                    val fileName = "${addedMessage.id}_$index$imageSuffix"

                    val dirPath =
                        storageRepository.getDataDirPath(appContext, Environment.DIRECTORY_PICTURES)
                            ?.let { it + appContext.resources.getString(R.string.message_attachment_dir) }

                    try {
                        storageRepository.saveAndUploadFile(
                            bucketId = "messages",
                            fileName = fileName,
                            dirPath = dirPath,
                            byteArray = byteArray
                        )
                    } catch (e: Exception) {
                        Log.d("debug_addMessage_error", e.toString())
                    } finally {
                        val newAttachment = NetworkAttachment(messageId = addedMessage.id, fileName = fileName)
                        messageNetworkDataSource.addAttachment(newAttachment).let {
                            val attachment = it.toDomainModel()
                            attachment.filePath = "$dirPath/$fileName"
                            attachmentDao.addAttachment(attachment)
                        }
                    }
                }
            }
        }
    }

    suspend fun markAllAsRead(userId: String) {
        messageDao.getUnreadMessages(userId).forEach {
            it.readAt = Clock.System.now()
            messageDao.upsertMessage(it)
            messageNetworkDataSource.updateMessage(messageId = it.id, timestamp = it.readAt)
        }
    }

    suspend fun populateMessagesToLocalDatabase(userId: String) {
        messageNetworkDataSource.getMessages(userId).forEach {
            messageDao.upsertMessage(it.toDomainModel(userId))
        }
        Log.d("debug_messages", "populated messages")
    }

    suspend fun syncMessages(currentUser: String) {
        Log.d("debug_messages", "syncing messages")
        messageNetworkDataSource.getMessageStream(currentUser).collect {
            messageDao.upsertMessage(it.toDomainModel(currentUser))
        }
    }
}