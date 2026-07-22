package me.linhvo.ittakestwo.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Pairing(
    @PrimaryKey val id: String,
    val userId: String,
    val partnerId: String?
)