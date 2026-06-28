package me.linhvo.ittakestwo.data

import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.linhvo.ittakestwo.model.User

class UserRepository(scope: CoroutineScope) {
    private val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""

    @OptIn(SupabaseExperimental::class)
    fun getUserInfo(): Flow<User?> =
        supabase.from("users").selectSingleValueAsFlow(User::id) {
            eq("id", userId)
        }

    @OptIn(SupabaseExperimental::class)
    fun getPartnerInfo(): Flow<User?> =
        flow {
            val partnerId = supabase.from("users").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<User>()?.partnerId

            Log.d("supabase_data", partnerId.toString())

            if (partnerId != null) {
                Log.d("supabase_data", "getting partner data flow...")
                emitAll(supabase.from("users").selectSingleValueAsFlow(User::id) {
                    eq("id", partnerId)
                })
            }
        }

}