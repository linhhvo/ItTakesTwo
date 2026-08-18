package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "message_attachments")
data class Attachment(
    @PrimaryKey val id: String,
    val messageId: String,
    val createdAt: Instant?,
    val fileName: String,
    var filePath: String? = null,
    var downloaded: Boolean = false
)