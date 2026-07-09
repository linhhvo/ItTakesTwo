package me.linhvo.ittakestwo.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import me.linhvo.ittakestwo.model.Message

class ChatRepository {
    val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
    val userRepository = UserRepository()

    suspend fun getChatMessages(): List<Message> =
        supabase.from("chat_messages").select {
            filter {
                or {
                    eq("sender", currentUserId)
                    eq("recipient", currentUserId)
                }
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<Message>().map {
            val senderName = userRepository.getUser(it.sender).displayName
            val recipientName = userRepository.getUser(it.sender).displayName

            it.copy(sender = senderName, recipient = recipientName, isSenderMe = it.sender == currentUserId)
        }
}