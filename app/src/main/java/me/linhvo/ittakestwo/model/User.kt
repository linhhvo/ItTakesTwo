package me.linhvo.ittakestwo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class Pairing(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("partner_id")
    val partnerId: String?
) {
    fun getPartnerId(currentUser: String): String? =
        if (currentUser == userId) partnerId
        else userId

}