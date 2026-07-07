package me.linhvo.ittakestwo.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class User(
    val id: String,
    val email: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("avatar_file")
    val avatarFile: String? = null,

    @SerialName("updated_at")
    val updatedAt: String,

    private var avatarUrl: String? = null
) {
    fun getInitial(): String = displayName.first().toString().uppercase()
    fun setAvatarUrl(url: String?) {
        avatarUrl = url
    }

    fun getAvatarUrl() = avatarUrl
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Pairing(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("partner_id")
    val partnerId: String? = null
) {
    fun getPartnerId(currentUser: String): String? =
        if (currentUser == userId) partnerId
        else if (partnerId != null) userId
        else null

}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class BroadcastResponse(
    val operation: String,
    val table: String,
    val record: JsonObject
)