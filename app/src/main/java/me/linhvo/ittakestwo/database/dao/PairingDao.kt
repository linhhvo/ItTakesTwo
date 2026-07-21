package me.linhvo.ittakestwo.database.dao

import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.Pairing

interface PairingDao {
    @Query("select * from pairing")
    fun loadPairing(): Flow<Pairing>

    @Update
    suspend fun update(pairing: Pairing)

}