package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import me.linhvo.ittakestwo.database.model.Pairing
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkPairing(
    val id: String? = null, // nullable so remote database can auto-generate ID

    @SerialName("user_id")
    val userId: String,

    @SerialName("partner_id")
    val partnerId: String,

    @SerialName("updated_at")
    val updatedAt: Instant
)

fun NetworkPairing.toDomainModel(currentUser: String): Pairing {
    val partner = if (currentUser == userId) partnerId
    else userId

    return Pairing(
        id = id!!,
        userId = currentUser,
        partnerId = partner,
        updatedAt = updatedAt
    )
}