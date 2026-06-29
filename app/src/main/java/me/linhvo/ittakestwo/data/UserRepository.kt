package me.linhvo.ittakestwo.data

import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.linhvo.ittakestwo.model.User

class UserRepository {
    private val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""

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


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserAndPartnerInfo(): Flow<Pair<User?, User?>> =
        getUserInfo().flatMapLatest { user ->
            if (user?.partnerId != null) {
                getPartnerInfo(user.partnerId).map { partner -> Pair(user, partner) }
            } else {
                flowOf(Pair(user, null))
            }
        }


    suspend fun addPartner(partnerEmail: String) {
        val partnerId = supabase.from("users").select {
            filter { eq("email", partnerEmail) }
        }.decodeSingleOrNull<User>()?.partnerId

        supabase.from("users").update({
            User::partnerId setTo partnerId
        }) {
            filter { eq("id", userId) }
        }
    }

}