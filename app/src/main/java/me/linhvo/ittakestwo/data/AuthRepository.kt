package me.linhvo.ittakestwo.data

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject

class AuthRepository {
    suspend fun signIn(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.d("Auth Error", e.message.toString())
            false
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("display_name", Json.parseToJsonElement(name))
                }
            }
            true
        } catch (e: Exception) {
            Log.d("Auth_Error", e.message.toString())
            false
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        supabase.auth.signOut()
    }
}