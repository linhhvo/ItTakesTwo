package me.linhvo.ittakestwo.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.AttachmentDao
import me.linhvo.ittakestwo.database.dao.MessageDao
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.network.datasource.MessageNetworkDataSource
import me.linhvo.ittakestwo.network.model.NetworkAttachment
import me.linhvo.ittakestwo.network.model.NetworkMessage
import me.linhvo.ittakestwo.network.model.toDomainModel
import java.nio.file.Files
import java.nio.file.Paths
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMessageListStream(): Flow<Map<Message, List<Attachment>?>> =
        messageDao.observeMessagesOrderByLatest().flatMapLatest { messages ->
            if (messages.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val attachments = messages.map { message ->
                    if (!message.attachments) {
                        flowOf(null)
                    } else {
                        attachmentDao.observeAttachments(message.id)
                    }
                }
                combine(attachments) { attachments ->
                    messages.zip(attachments).toMap()
                }
            }
        }

    fun addNewMessage(senderId: String, recipientId: String, content: String, byteArrays: Set<ByteArray?>) {
        val newMessage = NetworkMessage(
            senderId = senderId,
            recipientId = recipientId,
            content = content,
            attachments = byteArrays.isNotEmpty()
        )

        appScope.launch {
            val addedMessage = messageNetworkDataSource.addMessage(newMessage)

            val attachments = mutableListOf<Attachment>()

            if (byteArrays.isNotEmpty()) {
                byteArrays.forEachIndexed { index, byteArray ->
                    val fileName = "${addedMessage.id}_$index$imageSuffix"

                    storageRepository.saveAndUploadFile(
                        bucketId = "messages",
                        fileName = fileName,
                        dirPath = storageRepository.messageAttachmentDirPath,
                        byteArray = byteArray
                    )

                    messageNetworkDataSource.addAttachment(
                        NetworkAttachment(
                            messageId = addedMessage.id,
                            fileName = fileName
                        )
                    ).let {
                        val newAttachment = it.toDomainModel()
                        newAttachment.filePath = "${storageRepository.messageAttachmentDirPath}/${it.fileName}"
                        attachments += newAttachment
                    }
                }

                val message = addedMessage.toDomainModel(senderId)
                message.isSenderMe = true
                messageDao.upsertMessage(message)
                attachmentDao.insertAttachments(attachments)
                messageNetworkDataSource.setAttachmentsReady(addedMessage.id!!)
            }
        }
    }

    suspend fun markAllAsRead(userId: String) {
        messageDao.getUnreadMessages(userId).forEach {
            it.readAt = Clock.System.now()
            messageDao.upsertMessage(it)
            messageNetworkDataSource.updateMessageReadTime(messageId = it.id, timestamp = it.readAt)
        }
    }

    suspend fun downloadAttachments(attachments: List<Attachment>?) {
        if (!attachments.isNullOrEmpty()) {
            attachments.forEach {
                storageRepository.downloadAndSaveFile(
                    bucketId = "messages",
                    fileName = it.fileName,
                    dirPath = storageRepository.messageAttachmentDirPath
                )
            }
            attachmentDao.updateAttachments(attachments.map { it.copy(downloaded = true) })
        }
    }

    suspend fun populateMessagesToLocalDatabase(userId: String) {
        messageNetworkDataSource.getMessages(userId).forEach { networkMessage ->
            val message = networkMessage.toDomainModel(userId)

            if (networkMessage.attachments && networkMessage.attachmentsReady) {
                messageNetworkDataSource.getAttachments(message.id).forEach { networkAttachment ->
                    val attachment = networkAttachment.toDomainModel()

                    attachment.filePath = "${storageRepository.messageAttachmentDirPath}/${attachment.fileName}"
                    if (Files.exists(Paths.get(attachment.filePath))) {
                        attachment.downloaded = true
                    }
                    attachmentDao.insertAttachments(listOf(attachment))
                }
            }

            if (networkMessage.attachments == networkMessage.attachmentsReady) {
                messageDao.upsertMessage(message)
            }
        }
        Log.d("debug_messages", "populated messages")
    }

    suspend fun syncMessages(currentUser: String) {
        Log.d("debug_messages", "syncing messages")
        messageNetworkDataSource.getMessageStream(currentUser).collect { networkMessage ->
            val message = networkMessage.toDomainModel(currentUser)

            val newAttachments = mutableListOf<Attachment>()
            if (networkMessage.attachments && networkMessage.attachmentsReady) {
                messageNetworkDataSource.getAttachments(message.id).forEach { networkAttachment ->
                    if (networkAttachment.messageId == message.id) {
                        val attachment = networkAttachment.toDomainModel()
                        attachment.filePath = "${storageRepository.messageAttachmentDirPath}/${attachment.fileName}"

                        newAttachments += attachment
                    }
                }
                attachmentDao.insertAttachments(newAttachments)
                downloadAttachments(newAttachments)
            }

            if (networkMessage.attachments == networkMessage.attachmentsReady) {
                messageDao.upsertMessage(message)
            }
        }
    }
}