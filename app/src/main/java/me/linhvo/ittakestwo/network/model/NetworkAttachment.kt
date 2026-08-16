package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.linhvo.ittakestwo.database.model.Attachment

@Serializable
data class NetworkAttachment(
    val id: String? = null,

    @SerialName("message_id")
    val messageId: String?,

    @SerialName("file_name")
    val fileName: String
)

fun NetworkAttachment.toDomainModel() =
    Attachment(
        id = id!!,
        messageId = messageId!!,
        fileName = fileName
    )