package me.linhvo.ittakestwo.data

import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import me.linhvo.ittakestwo.model.Pairing
import me.linhvo.ittakestwo.model.User

class UserRepository(val uiScope: CoroutineScope) {
    private val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""

//    @OptIn(SupabaseExperimental::class)
//    fun getUserInfo(): Flow<User?> {
//        val channel = supabase.channel("user:$userId")
//        return channelFlow {
//            val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
//                table = "users"
//                filter("id", FilterOperator.EQ, userId)
//            }
//
//            changeFlow.onEach {
//                Log.d("supabase", it.toString())
//                send(Json.decodeFromJsonElement<User>(it.record))
//            }.launchIn(this)
//
//            channel.subscribe()
//        }
//            .onCompletion {
//                channel.unsubscribe()
//            }
//    }

    @OptIn(SupabaseExperimental::class)
    fun getUserInfo(): Flow<User?> =
        supabase.from("users").selectSingleValueAsFlow(User::id) {
            eq("id", userId)
        }

    @OptIn(SupabaseExperimental::class)
    fun getPartnerInfo(partnerId: String): Flow<User?> =
        supabase.from("users").selectSingleValueAsFlow(User::id) {
            eq("id", partnerId)
        }

    suspend fun getPairing(userId: String): Pairing? =
        supabase.from("pairings").select {
            filter {
                or {
                    eq("user_id", userId)
                    eq("partner_id", userId)
                }
            }
        }.decodeSingleOrNull<Pairing>()

    @OptIn(SupabaseExperimental::class)
    fun getPairingByUserId(userId: String) =
        supabase.from("pairings")
            .selectAsFlow(Pairing::id, filter = FilterOperation("user_id", FilterOperator.EQ, userId))

    @OptIn(SupabaseExperimental::class)
    fun getPairingByPartnerId(userId: String) =
        supabase.from("pairings")
            .selectAsFlow(Pairing::id, filter = FilterOperation("partner_id", FilterOperator.EQ, userId))

    @OptIn(SupabaseExperimental::class)
    fun getPairingFlow(userId: String): Flow<List<Pairing>> =
        combine(getPairingByUserId(userId), getPairingByPartnerId(userId)) { pairings, pairings1 ->
            pairings + pairings1
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserAndPartnerInfo(): Flow<Pair<User?, User?>> =
        getUserInfo().flatMapLatest { user ->
            getPairingFlow(userId).flatMapLatest { pairings ->
                Log.d("supabase", pairings.toString())
                val partnerId = pairings.first().getPartnerId(userId)
                if (partnerId != null) {
                    getPartnerInfo(partnerId).map { partner ->
                        Pair(user, partner)
                    }
                } else {
                    flowOf(Pair(user, null))
                }
            }
        }


    suspend fun addPartner(partnerEmail: String) = withContext(Dispatchers.IO) {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<User>()?.id
        if (partnerId == null) {
            throw Exception("No account exists for this email.")
        }

        val pairing = getPairing(userId)
        if (pairing == null) {
            supabase.from("pairings").insert(Pairing(userId = userId, partnerId = partnerId))
        } else {
            if (userId == pairing.userId) {
                supabase.from("pairings").update({
                    set("partner_id", partnerId)
                }) {
                    filter { eq("id", pairing.id!!) }
                }
            }
        }
    }
}