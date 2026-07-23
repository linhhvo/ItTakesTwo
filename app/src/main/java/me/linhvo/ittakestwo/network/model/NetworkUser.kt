package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject
import me.linhvo.ittakestwo.database.model.User
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkUser(
    val id: String,
    val email: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("avatar_file")
    val avatarFile: String? = null,

    @SerialName("updated_at")
    val updatedAt: Instant,

    )

fun NetworkUser.toDomainModel() = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarFile = avatarFile,
    updatedAt = updatedAt
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class BroadcastResponse(
    val operation: String,
    val table: String,
    val record: JsonObject
)