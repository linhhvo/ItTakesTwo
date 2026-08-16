package me.linhvo.ittakestwo.network.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.network.model.BroadcastResponse
import me.linhvo.ittakestwo.network.model.NetworkAttachment
import me.linhvo.ittakestwo.network.model.NetworkMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class MessageNetworkDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) {

    suspend fun getMessages(userId: String): List<NetworkMessage> =
        supabase.from("chat_messages").select {
            filter {
                or {
                    eq("sender_id", userId)
                    eq("recipient_id", userId)
                }
            }
            order(column = "sent_at", order = Order.DESCENDING)
        }.decodeList<NetworkMessage>()

    @OptIn(SupabaseExperimental::class)
    fun getMessageStream(userId: String): Flow<NetworkMessage> {
        val channel = supabase.channel("chat:$userId") { isPrivate = true }
        return flow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "chat_message")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                val message = Json.decodeFromJsonElement<NetworkMessage>(res.record)

                emit(message)
            }.collect()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    suspend fun addMessage(message: NetworkMessage): NetworkMessage =
        supabase.from("chat_messages").insert(message) {
            select()
        }.decodeSingle<NetworkMessage>()

    suspend fun updateMessageReadTime(messageId: String, timestamp: Instant?) {
        supabase.from("chat_messages").update({
            set("read_at", timestamp)
        }) {
            filter { eq("id", messageId) }
        }
    }

    suspend fun setAttachmentsReady(messageId: String) {
        supabase.from("chat_messages").update({
            set("attachments_ready", true)
        }) {
            filter { eq("id", messageId) }
        }
    }

    suspend fun addAttachment(attachment: NetworkAttachment): NetworkAttachment =
        supabase.from("message_attachments").insert(attachment) {
            select()
        }.decodeSingle<NetworkAttachment>()

    suspend fun getAttachments(messageId: String): List<NetworkAttachment> =
        supabase.from("message_attachments").select {
            filter { eq("message_id", messageId) }
        }.decodeList<NetworkAttachment>()

}