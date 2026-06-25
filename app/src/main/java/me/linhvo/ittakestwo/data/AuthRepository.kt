package me.linhvo.ittakestwo.data

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import me.linhvo.ittakestwo.model.User

class AuthRepository {
    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        runCatching {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        runCatching {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("display_name", Json.parseToJsonElement(name))
                }
            }
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching {
            supabase.auth.signOut()
        }
    }

    suspend fun getUser(): User? {
        val currentUser = supabase.auth.currentSessionOrNull()?.user
        if (currentUser != null) {
            return supabase.from("users").select() {
                filter {
                    eq("id", currentUser.id)
                }
            }.decodeSingle<User>()
        }
        return null
    }

    fun getSession(): StateFlow<SessionStatus> {
        Log.d("auth_session", "session status is ${supabase.auth.sessionStatus.value} ")
        return supabase.auth.sessionStatus
    }
}