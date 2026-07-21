package me.linhvo.ittakestwo.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import me.linhvo.ittakestwo.database.dao.MessageDao
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.database.model.Pairing
import me.linhvo.ittakestwo.database.model.User

@Database(
    entities = [
        User::class,
        Pairing::class,
        Message::class
    ],
    version = 1
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pairingDao(): PairingDao
    abstract fun messageDao(): MessageDao
}