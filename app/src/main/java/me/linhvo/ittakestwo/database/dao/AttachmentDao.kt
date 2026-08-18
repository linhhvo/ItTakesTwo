package me.linhvo.ittakestwo.database.dao

import androidx.room3.*
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.Attachment

@Dao
interface AttachmentDao {
    @Query("select * from message_attachments where messageId = :messageId")
    suspend fun loadAttachments(messageId: String): List<Attachment>

    @Query("select * from message_attachments where id = :attachmentId")
    suspend fun loadAttachment(attachmentId: String): Attachment?

    @Query("select * from message_attachments where messageId = :messageId")
    fun observeAttachments(messageId: String): Flow<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttachments(attachments: List<Attachment>)

    @Update
    suspend fun updateAttachments(attachments: List<Attachment>)
}