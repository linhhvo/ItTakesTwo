package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.linhvo.ittakestwo.database.model.Attachment
import kotlin.time.Instant

@Serializable
data class NetworkAttachment(
    val id: String? = null,

    @SerialName("message_id")
    val messageId: String?,

    @SerialName("file_name")
    val fileName: String,

    @SerialName("created_at")
    val createdAt: Instant? = null
)

fun NetworkAttachment.toDomainModel() =
    Attachment(
        id = id!!,
        messageId = messageId!!,
        fileName = fileName,
        createdAt = createdAt
    )