package me.linhvo.ittakestwo.network.datasource

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
import me.linhvo.ittakestwo.network.model.BroadcastResponse
import me.linhvo.ittakestwo.network.model.NetworkPairing
import me.linhvo.ittakestwo.network.model.NetworkUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingNetworkDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) {

    //TODO: keep track of when the background is updated and only get new url if changed
//    suspend fun getBackgroundUrlFromNet(): String? = withContext(Dispatchers.IO) {
//        val pairingId = getPairing(currentUserId).id
//        try {
//            supabase.storage.from("backgrounds").createSignedUrl(path = "$pairingId.png", expiresIn = 1.hours)
//        } catch (e: Exception) {
//            Log.d("debug_background", e.message.toString())
//            null
//        }
//    }

    suspend fun getPairing(userId: String): NetworkPairing = withContext(Dispatchers.IO) {
        supabase.from("pairings").select {
            filter {
                or {
                    eq("user_id", userId)
                    eq("partner_id", userId)
                }
            }
        }.decodeSingle<NetworkPairing>()
    }

    @OptIn(SupabaseExperimental::class)
    fun getPairingStream(userId: String): Flow<NetworkPairing> {
        val channel = supabase.channel("pairing:$userId") { isPrivate = true }
        return flow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "pairing_changes")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                emit(Json.decodeFromJsonElement<NetworkPairing>(res.record))
            }.collect()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProfileStream(userId: String): Flow<Pair<NetworkUser, NetworkUser?>> {
        val userNetworkDataSource = UserNetworkDataSource(supabase)
        val userFlow = userNetworkDataSource.getUserStream(userId = userId)

        val partnerFlow = getPairingStream(userId).flatMapLatest {
//            val partnerId = it.getPartnerId(userId)
            val partnerId = it.partnerId
            if (partnerId != null) {
                userNetworkDataSource.getUserStream(userId = partnerId)
            } else {
                flowOf(null)
            }
        }

        return combine(userFlow, partnerFlow) { user, partner ->
            Pair(user, partner)
        }
    }

    suspend fun getProfileInfo(userId: String): Pair<NetworkUser, NetworkUser?> {
        val userNetworkDataSource = UserNetworkDataSource(supabase)
        val user = userNetworkDataSource.getUser(userId)
//        val partnerId = getPairing(userId).getPartnerId(userId)
        val partnerId = getPairing(userId).partnerId
        if (partnerId != null) {
            val partner = userNetworkDataSource.getUser(userId = partnerId)
            return Pair(user, partner)
        } else {
            return Pair(user, null)
        }
    }

    suspend fun addPartner(userId: String, partnerEmail: String) = withContext(Dispatchers.IO) {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<NetworkUser>()?.id
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