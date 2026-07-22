package me.linhvo.ittakestwo.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.Pairing

@Dao
interface PairingDao {
    @Query("select * from pairing")
    fun getPairing(): Pairing?

    @Query("select * from pairing")
    fun observePairing(): Flow<Pairing?>

    @Upsert
    suspend fun upsert(pairing: Pairing)
}