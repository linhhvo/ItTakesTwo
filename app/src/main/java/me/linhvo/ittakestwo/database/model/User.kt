package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant


@Entity
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    var avatarFile: String? = null,
    var updatedAt: Instant
) {
    val initial: String
        get() = displayName.first().uppercase()
}