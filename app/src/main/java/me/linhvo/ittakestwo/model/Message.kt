package me.linhvo.ittakestwo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val sender: String,
    val recipient: String,
    val content: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("sent_at")
    val sentAt: String,

    @SerialName("read_at")
    val readAt: String?,

    var isSenderMe: Boolean = false
) {
}