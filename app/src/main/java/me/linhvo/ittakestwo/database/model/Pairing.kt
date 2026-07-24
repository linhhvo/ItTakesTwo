package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import me.linhvo.ittakestwo.network.model.NetworkPairing
import kotlin.time.Instant

@Entity
data class Pairing(
    @PrimaryKey val id: String,
    val userId: String,
    val partnerId: String,
    val updatedAt: Instant
)

fun Pairing.toNetworkModel() =
    NetworkPairing(
        id = id,
        userId = userId,
        partnerId = partnerId,
        updatedAt = updatedAt
    )