package me.linhvo.ittakestwo.network.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.network.model.NetworkPairing
import me.linhvo.ittakestwo.network.model.NetworkUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

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

    suspend fun getPairing(userId: String): NetworkPairing? =
        supabase.from("pairings").select {
            filter {
                or {
                    eq("user_id", userId)
                    eq("partner_id", userId)
                }
            }
        }.decodeSingleOrNull<NetworkPairing>()

    suspend fun updatePairing(pairing: NetworkPairing) =
        supabase.from("pairings").upsert(pairing)

    suspend fun addNewPairing(userId: String, partnerEmail: String): NetworkPairing {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<NetworkUser>()?.id
        if (partnerId == null) {
            throw Exception("No account exists for this email.")
        }

        val newPairing = NetworkPairing(userId = userId, partnerId = partnerId, updatedAt = Clock.System.now())
        return supabase.from("pairings").insert(newPairing) {
            select()
        }.decodeSingle<NetworkPairing>()
    }

    @OptIn(SupabaseExperimental::class)
    fun getPairingStream(userId: String): Flow<NetworkPairing> {
        val channel = supabase.channel("pairing:$userId") { isPrivate = true }
        return flow {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "pairings" }

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach {
                if (it is PostgresAction.Insert) {
                    val pairing = Json.decodeFromJsonElement<NetworkPairing>(it.record)
                    emit(pairing)
                }
                //TODO: also need to emit when pairing is removed
            }.collect()
        }.onCompletion {
            channel.unsubscribe()
        }
    }


}