package me.linhvo.ittakestwo.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Message(
    val id: String,
    val sender: String,
    val recipient: String,
    val content: String,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("sent_at")
    val sentAt: Instant,

    @SerialName("read_at")
    val readAt: Instant?,

    var isSenderMe: Boolean = false
) {
    fun parseDateTime(instant: Instant) =
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
}