package me.linhvo.ittakestwo.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
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
    suspend fun loadMessagesOrderByLatest(): List<Message>

    @Query(
        """
        select * from chat_messages  
        order by sentAt desc
    """
    )
    fun observeMessagesOrderByLatest(): Flow<List<Message>>

    @Upsert
    suspend fun upsert(message: Message)

//    @Query(
//        """
//        update chat_messages set readAt = :timestamp
//        where recipientId = :userId
//    """
//    )
//    suspend fun updateReadTime(timestamp: Instant, userId: String): List<Message>

    @Query(
        """
        select * from chat_messages
        where recipientId = :userId and readAt is null
    """
    )
    suspend fun getUnreadMessages(userId: String): List<Message>
}