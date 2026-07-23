package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "chat_messages")
data class Message(
    @PrimaryKey val id: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val sentAt: Instant? = null,
    val readAt: Instant? = null,
    var isSenderMe: Boolean = false
)