package me.linhvo.ittakestwo.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

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

    suspend fun getPairing(userId: String): Pairing = withContext(Dispatchers.IO) {
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
    fun getPairingStream(userId: String): Flow<Pairing> {
        val channel = supabase.channel("pairing:$userId") { isPrivate = true }
        return channelFlow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "pairing_changes")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                send(Json.decodeFromJsonElement<Pairing>(res.record))
            }.launchIn(this)
        }.onStart {
            emit(getPairing(userId))
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProfileStream(userId: String): Flow<Pair<User, User?>> {
        val userRepository = UserRepository(supabase)
        val userFlow = userRepository.getUserStream(role = Role.USER, channelId = userId, userId = userId)

        val partnerFlow = getPairingStream(userId).flatMapLatest {
            val partnerId = it.getPartnerId(userId)
            if (partnerId != null) {
                userRepository.getUserStream(role = Role.PARTNER, channelId = userId, userId = partnerId)
            } else {
                flowOf(null)
            }
        }

        return combine(userFlow, partnerFlow) { user, partner ->
            Pair(user, partner)
        }
    }

    suspend fun getProfileInfo(userId: String): Pair<User, User?> {
        val userRepository = UserRepository(supabase)
        val user = userRepository.getUser(userId)
        val partnerId = getPairing(userId).getPartnerId(userId)
        if (partnerId != null) {
            val partner = userRepository.getUser(userId = partnerId)
            return Pair(user, partner)
        } else {
            return Pair(user, null)
        }
    }

    suspend fun addPartner(userId: String, partnerEmail: String) = withContext(Dispatchers.IO) {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<User>()?.id
        if (partnerId == null) {
            throw Exception("No account exists for this email.")
        }

        supabase.from("pairings").update({
            set("partner_id", partnerId)
        }) {
            filter { eq("user_id", userId) }
        }
    }

}