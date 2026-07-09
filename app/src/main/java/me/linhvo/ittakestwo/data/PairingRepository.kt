package me.linhvo.ittakestwo.data

import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.model.BroadcastResponse
import me.linhvo.ittakestwo.model.Pairing
import me.linhvo.ittakestwo.model.User
import kotlin.time.Duration.Companion.hours

class PairingRepository {
    private val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""

    //TODO: keep track of when the background is updated and only get new url if changed
    suspend fun getBackgroundUrlFromNet(): String? = withContext(Dispatchers.IO) {
        val pairingId = getPairing(currentUserId).id
        try {
            supabase.storage.from("backgrounds").createSignedUrl(path = "$pairingId.png", expiresIn = 1.hours)
        } catch (e: Exception) {
            Log.d("debug_background", e.message.toString())
            null
        }
    }

    suspend fun getPairing(userId: String = currentUserId): Pairing = withContext(Dispatchers.IO) {
        supabase.from("pairings").select {
            filter {
                or {
                    eq("user_id", userId)
                    eq("partner_id", userId)
                }
            }
        }.decodeSingle<Pairing>()
    }

    @OptIn(SupabaseExperimental::class)
    fun getPairingStream(): Flow<Pairing> {
        val channel = supabase.channel("pairing:$currentUserId") { isPrivate = true }
        return channelFlow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "pairing_changes")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                send(Json.decodeFromJsonElement<Pairing>(res.record))
            }.launchIn(this)
        }.onStart {
            emit(getPairing())
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProfileStream(): Flow<Pair<User, User?>> {
        val userRepository = UserRepository()
        val userFlow = userRepository.getUserStream(role = Role.USER, userId = currentUserId)

        val partnerFlow = getPairingStream().flatMapLatest {
            val partnerId = it.getPartnerId(currentUserId)
            if (partnerId != null) {
                userRepository.getUserStream(role = Role.PARTNER, userId = partnerId)
            } else {
                flowOf(null)
            }
        }

        return combine(userFlow, partnerFlow) { user, partner ->
            Pair(user, partner)
        }
    }

    suspend fun getProfileInfo(): Pair<User, User?> {
        val userRepository = UserRepository()
        val user = userRepository.getUser(currentUserId)
        val partnerId = getPairing().getPartnerId(currentUserId)
        if (partnerId != null) {
            val partner = userRepository.getUser(userId = partnerId)
            return Pair(user, partner)
        } else {
            return Pair(user, null)
        }
    }

    suspend fun addPartner(partnerEmail: String) = withContext(Dispatchers.IO) {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<User>()?.id
        if (partnerId == null) {
            throw Exception("No account exists for this email.")
        }

        supabase.from("pairings").update({
            set("partner_id", partnerId)
        }) {
            filter { eq("user_id", currentUserId) }
        }
    }

}