package me.linhvo.ittakestwo.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Message(
    val id: String? = null,
    val sender: String,
    val recipient: String,
    val content: String,

    @SerialName("sent_at")
    val sentAt: Instant? = null,

    @SerialName("read_at")
    val readAt: Instant? = null,

    var isSenderMe: Boolean = false
) {
    fun parseDateTime(instant: Instant) =
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
}