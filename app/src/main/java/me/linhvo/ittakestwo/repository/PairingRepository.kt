package me.linhvo.ittakestwo.repository

import android.util.Log
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.network.datasource.PairingNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val pairingNetworkDataSource: PairingNetworkDataSource,
    private val pairingDao: PairingDao,
) {

    suspend fun syncPairing(currentUser: String) {
        pairingNetworkDataSource.getPairingStream(currentUser).collect { pairing ->
            Log.d("debug_syncPairing", pairing.toString())
            pairing.toDomainModel(currentUser).let {
                pairingDao.upsert(it)
            }
        }
    }
}