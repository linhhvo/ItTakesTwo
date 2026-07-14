package me.linhvo.ittakestwo.data

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import me.linhvo.ittakestwo.model.Message

class ChatRepository {
    private val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
    private val userRepository = UserRepository()
    private val pairingRepository = PairingRepository()

    suspend fun getMessages(): List<Message> = withContext(Dispatchers.IO) {
        supabase.from("chat_messages").select {
            filter {
                or {
                    eq("sender", currentUserId)
                    eq("recipient", currentUserId)
                }
            }
            order(column = "sent_at", order = Order.DESCENDING)
        }.decodeList<Message>().map {
            val senderName = userRepository.getUser(it.sender).displayName
            val recipientName = userRepository.getUser(it.sender).displayName

            it.copy(sender = senderName, recipient = recipientName, isSenderMe = it.sender == currentUserId)
        }
}