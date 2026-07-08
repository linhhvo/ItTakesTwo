package me.linhvo.ittakestwo.data

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.model.BroadcastResponse
import me.linhvo.ittakestwo.model.User
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.hours

enum class Role(val text: String) {
    USER("user"),
    PARTNER("partner")
}

class UserRepository {
    private val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""

    suspend fun getUser(userId: String): User =
        supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingle<User>()

    @OptIn(SupabaseExperimental::class)
    fun getUserStream(role: Role, userId: String): Flow<User> {
        val channel = supabase.channel("${role.text}:$currentUserId") { isPrivate = true }
        return channelFlow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "UPDATE")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                val user = Json.decodeFromJsonElement<User>(res.record)

                val avatarUrl = getAvatarUrlFromNet(user.avatarFile)
                user.setAvatarUrl(avatarUrl)
                Log.d("debug_flow", "sending $role flow value...")
                send(user)
            }.launchIn(this)
        }.onStart {
            val initialData = withContext(Dispatchers.IO) {
                getUser(userId)
            }
            val avatarUrl = getAvatarUrlFromNet(initialData.avatarFile)
            initialData.setAvatarUrl(avatarUrl)
            Log.d("debug_flow", "initiating $role data...")

            emit(initialData)
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    suspend fun uploadUserAvatar(context: Context, avatarUri: Uri) = withContext(Dispatchers.IO) {
        val signedUrl = supabase.storage.from("avatars").createSignedUploadUrl("$currentUserId.png", upsert = true)
        val byteArray = context.contentResolver.openInputStream(avatarUri)?.use { it.buffered().readBytes() }

        try {
            supabase.storage.from("avatars")
                .uploadToSignedUrl(path = "$currentUserId.png", token = signedUrl.token, data = byteArray!!) {
                    upsert = true
                }
        } finally {
            val user = supabase.from("users").select {
                filter { eq("id", currentUserId) }
            }.decodeSingle<User>()
            if (user.avatarFile == null) {
                supabase.from("users").update({
                    set("avatar_file", "$currentUserId.png")
                }) {
                    filter { eq("id", currentUserId) }
                }
            }

            supabase.from("users").update({
                set(
                    "updated_at", DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                )
            }) {
                filter { eq("id", currentUserId) }
            }

        }
    }

    suspend fun getAvatarUrlFromNet(avatarFile: String?): String? = withContext(Dispatchers.IO) {
        Log.d("debug_avatar", "creating new url for $avatarFile...")
        if (avatarFile != null) {
            supabase.storage.from("avatars").createSignedUrl(path = avatarFile, expiresIn = 1.hours)
        } else {
            null
        }
    }
}