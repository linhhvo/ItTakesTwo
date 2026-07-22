package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    var avatarFile: String? = null,
    var updatedAt: Long
) {
    val initial: String
        get() = displayName.first().uppercase()
}