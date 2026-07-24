package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.linhvo.ittakestwo.database.model.Message
import kotlin.time.Instant

@Serializable
data class NetworkMessage(
    val id: String? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("recipient_id")
    val recipientId: String,
    val content: String,

    @SerialName("sent_at")
    val sentAt: Instant? = null,

    @SerialName("read_at")
    val readAt: Instant? = null,
)

fun NetworkMessage.toDomainModel(currentUser: String) =
    Message(
        id = id!!,
        senderId = senderId,
        recipientId = recipientId,
        content = content,
        sentAt = sentAt,
        readAt = readAt,
        isSenderMe = currentUser == senderId
    )