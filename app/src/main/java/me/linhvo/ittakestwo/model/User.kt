package me.linhvo.ittakestwo.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject

@Serializable
data class User(
    val id: String,

    val email: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,
) {
    fun getInitial(): String = displayName.first().toString().uppercase()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Pairing(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("partner_id")
    val partnerId: String?
) {
    fun getPartnerId(currentUser: String): String? =
        if (currentUser == userId) partnerId
        else if (partnerId != null) userId
        else null

}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Avatar(
    val name: String,
    val owner: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class BroadcastResponse(
    val operation: String,
    val table: String,
    val record: JsonObject
)