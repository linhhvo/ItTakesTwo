package me.linhvo.ittakestwo.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.Message

@Dao
interface MessageDao {
    @Query(
        """
        select * from chat_messages  
        order by sentAt desc
    """
    )
    fun observeMessagesOrderByLatest(): Flow<List<Message>>

    @Insert
    suspend fun insert(message: Message)

    @Query(
        """
        update chat_messages set readAt = :timestamp
        where recipientId = :userId
    """
    )
    suspend fun markAllAsRead(timestamp: Long, userId: String)

    @Query(
        """
        select count(id) from chat_messages
        where recipientId = :userId and readAt is null
    """
    )
    suspend fun getUnreadCount(userId: String): Int
}