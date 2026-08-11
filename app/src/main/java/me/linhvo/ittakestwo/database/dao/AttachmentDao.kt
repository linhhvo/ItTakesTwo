package me.linhvo.ittakestwo.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.Attachment

@Dao
interface AttachmentDao {
    @Query("select * from message_attachments where messageId = :messageId")
    suspend fun loadAttachments(messageId: String): List<Attachment>

    @Query("select * from message_attachments where messageId = :messageId")
    fun observeAttachments(messageId: String): Flow<List<Attachment>>

    @Upsert
    suspend fun addAttachment(attachment: Attachment)
}