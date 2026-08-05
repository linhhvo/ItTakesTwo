package me.linhvo.ittakestwo.network.datasource

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
import me.linhvo.ittakestwo.network.model.BroadcastResponse
import me.linhvo.ittakestwo.network.model.NetworkUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

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

                emit(user)
            }.collect()
        }.onCompletion {
            channel.unsubscribe()
        }
    }

    suspend fun upsertUser(user: NetworkUser) {
        supabase.from("users").upsert(user)
    }

    suspend fun updateUserFid(userId: String, fid: String, fcmToken: String) {
        supabase.from("users").update({
            set("fcm_token", fcmToken)
            set("fid", fid)
            set("updated_at", Clock.System.now())
        }) {
            filter { eq("id", userId) }
        }
    }

    suspend fun uploadUserAvatar(fileName: String, byteArray: ByteArray?) {
        val signedUrl = supabase.storage.from("avatars").createSignedUploadUrl(fileName, upsert = true)

        byteArray?.let {
            supabase.storage.from("avatars")
                .uploadToSignedUrl(path = fileName, token = signedUrl.token, data = byteArray) {
                    upsert = true
                }
        }
    }

    suspend fun getAvatarUrlFromNet(avatarFile: String?): String? {
        return if (avatarFile != null) {
            supabase.storage.from("avatars").createSignedUrl(path = avatarFile, expiresIn = 1.hours)
        } else {
            null
        }
    }
}