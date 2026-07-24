package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import me.linhvo.ittakestwo.network.model.NetworkMessage
import kotlin.time.Instant

@Entity(tableName = "chat_messages")
data class Message(
    @PrimaryKey val id: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val sentAt: Instant? = null,
    var readAt: Instant? = null,
    var isSenderMe: Boolean
)

fun Message.toNetworkModel() =
    NetworkMessage(
        id = id,
        senderId = senderId,
        recipientId = recipientId,
        content = content,
        sentAt = sentAt,
        readAt = readAt
    )