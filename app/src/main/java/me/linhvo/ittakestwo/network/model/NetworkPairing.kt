package me.linhvo.ittakestwo.network.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import me.linhvo.ittakestwo.database.model.Pairing

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkPairing(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("partner_id")
    val partnerId: String? = null
)

fun NetworkPairing.toDomainModel(currentUser: String): Pairing {
    val partner = if (currentUser == userId) partnerId
    else if (partnerId != null) userId
    else null

    return Pairing(
        id = this.id,
        userId = currentUser,
        partnerId = partner
    )
}