package me.linhvo.ittakestwo.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val supabase: SupabaseClient) {
    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("display_name", Json.parseToJsonElement(name))
            }
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        supabase.auth.signOut()
    }


    fun getSession(): StateFlow<SessionStatus> {
        return supabase.auth.sessionStatus
    }

    val currentUserId: String?
        get() = supabase.auth.currentSessionOrNull()?.user?.id

}