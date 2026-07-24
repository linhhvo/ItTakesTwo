package me.linhvo.ittakestwo.network.datasource

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
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
import me.linhvo.ittakestwo.network.model.BroadcastResponse
import me.linhvo.ittakestwo.network.model.NetworkMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class MessageNetworkDataSource @Inject constructor(
    private val supabase: SupabaseClient,
    private val pairingNetworkDataSource: PairingNetworkDataSource,
) {

    suspend fun getMessages(userId: String): List<NetworkMessage> = withContext(Dispatchers.IO) {
        supabase.from("chat_messages").select {
            filter {
                or {
                    eq("sender", userId)
                    eq("recipient", userId)
                }
            }
            order(column = "sent_at", order = Order.DESCENDING)
        }.decodeList<NetworkMessage>()
    }

    @OptIn(SupabaseExperimental::class)
    fun getMessageStream(userId: String): Flow<NetworkMessage> {
        val channel = supabase.channel("chat:$userId") { isPrivate = true }
        return channelFlow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "chat_message")

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                val message = Json.decodeFromJsonElement<NetworkMessage>(res.record)

//                Log.d("debug_newMessageInRepo", message.toString())
                send(message)
            }.launchIn(this)

            channel.subscribe(blockUntilSubscribed = true)
        }.onCompletion {
            channel.unsubscribe()
            Log.d("debug_channel", "channel unsubscribed")
        }
    }

    suspend fun addMessage(userId: String, content: String) = withContext(Dispatchers.IO) {
        val message = NetworkMessage(
            sender = userId,
            recipient = pairingNetworkDataSource.getPairing(userId)!!.partnerId,
            content = content
        )
        supabase.from("chat_messages").insert(message)
    }

    suspend fun getUnreadCount(userId: String) = withContext(Dispatchers.IO) {
        supabase.from("chat_messages").select {
            filter {
                and {
                    eq("recipient", userId)
                    exact("read_at", null)
                }
            }
            count(Count.EXACT)
        }.countOrNull() ?: 0
    }

    suspend fun updateReadTime(userId: String) = withContext(Dispatchers.IO) {
        if (getUnreadCount(userId) > 0) {
//            Log.d("debug_readTime", "updating message read time to ${Clock.System.now()}")
            supabase.from("chat_messages").update({
                set("read_at", Clock.System.now())
            }) {
                filter {
                    and {
                        eq("recipient", userId)
                        exact("read_at", null)
                    }
                }
            }
        }
    }
}