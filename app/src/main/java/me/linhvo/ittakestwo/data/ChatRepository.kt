package me.linhvo.ittakestwo.data

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.model.BroadcastResponse
import me.linhvo.ittakestwo.model.Message
import kotlin.time.Clock

class ChatRepository {
    private val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
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
            it.copy(isSenderMe = it.sender == currentUserId)
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun getMessageStream(): Flow<Message> {
        val channel = supabase.channel("chat:$currentUserId") { isPrivate = true }
        return channelFlow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "chat_message")

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                val message = Json.decodeFromJsonElement<Message>(res.record).apply {
                    isSenderMe = this.sender == currentUserId
                }

//                Log.d("debug_newMessageInRepo", message.toString())
                send(message)
            }.launchIn(this)

            channel.subscribe(blockUntilSubscribed = true)
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    suspend fun addMessage(content: String) = withContext(Dispatchers.IO) {
        val message = Message(
            sender = currentUserId,
            recipient = pairingRepository.getPairing().getPartnerId(currentUserId)!!,
            content = content
        )
        supabase.from("chat_messages").insert(message)
    }

    suspend fun getUnreadCount() = withContext(Dispatchers.IO) {
        supabase.from("chat_messages").select {
            filter {
                and {
                    eq("recipient", currentUserId)
                    exact("read_at", null)
                }
            }
            count(Count.EXACT)
        }.countOrNull() ?: 0
    }

    suspend fun updateReadTime() = withContext(Dispatchers.IO) {
        if (getUnreadCount() > 0) {
//            Log.d("debug_readTime", "updating message read time to ${Clock.System.now()}")
            supabase.from("chat_messages").update({
                set("read_at", Clock.System.now())
            }) {
                filter {
                    and {
                        eq("recipient", currentUserId)
                        exact("read_at", null)
                    }
                }
            }
        }
    }
}