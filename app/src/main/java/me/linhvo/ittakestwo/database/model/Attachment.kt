package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "message_attachments")
data class Attachment(
    @PrimaryKey val id: String,
    val messageId: String,
    val fileName: String,
    var filePath: String? = null
)