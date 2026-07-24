package me.linhvo.ittakestwo.network.datasource

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.network.model.BroadcastResponse
import me.linhvo.ittakestwo.network.model.NetworkUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

enum class Role(val text: String) {
    USER("user"),
    PARTNER("partner")
}

@Singleton
class UserNetworkDataSource @Inject constructor(val supabase: SupabaseClient) {

    suspend fun getUser(userId: String): NetworkUser? =
        supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<NetworkUser>()

    @OptIn(SupabaseExperimental::class)
    fun getUserStream(userId: String): Flow<NetworkUser> {
        val channel = supabase.channel("user:$userId") { isPrivate = true }
        return flow {
            val changeFlow = channel.broadcastFlow<JsonObject>(event = "UPDATE")

            channel.subscribe(blockUntilSubscribed = true)

            changeFlow.onEach { payload ->
                val res = Json.decodeFromJsonElement<BroadcastResponse>(payload)
                val user = Json.decodeFromJsonElement<NetworkUser>(res.record)

//                val avatarUrl = getAvatarUrlFromNet(user.avatarFile)
//                user.setAvatarUrl(avatarUrl)
                emit(user)
            }.collect()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    suspend fun upsertUser(user: NetworkUser) {
        supabase.from("users").upsert(user)
    }

    suspend fun uploadUserAvatar(userId: String, context: Context, avatarUri: Uri) {
        val signedUrl = supabase.storage.from("avatars").createSignedUploadUrl("$userId.png", upsert = true)
        val byteArray = context.contentResolver.openInputStream(avatarUri)?.use { it.buffered().readBytes() }

        try {
            supabase.storage.from("avatars")
                .uploadToSignedUrl(path = "$userId.png", token = signedUrl.token, data = byteArray!!) {
                    upsert = true
                }
        } finally {
            val user = supabase.from("users").select {
                filter { eq("id", userId) }
            }.decodeSingle<User>()
            if (user.avatarFile == null) {
                supabase.from("users").update({
                    set("avatar_file", "$userId.png")
                }) {
                    filter { eq("id", userId) }
                }
            }

            supabase.from("users").update({
                set("updated_at", Clock.System.now())
            }) {
                filter { eq("id", userId) }
            }

        }
    }

    suspend fun getAvatarUrlFromNet(avatarFile: String?): String? =
//        Log.d("debug_avatar", "creating new url for $avatarFile...")
        if (avatarFile != null) {
            supabase.storage.from("avatars").createSignedUrl(path = avatarFile, expiresIn = 1.hours)
        } else {
            null
        }
}