package me.linhvo.ittakestwo.database

import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.network.datasource.PairingNetworkDataSource
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncRepository @Inject constructor(
    private val userDao: UserDao,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val pairingNetworkDataSource: PairingNetworkDataSource,
    private val pairingDao: PairingDao
) {
    suspend fun initializeData(currentUser: String) {
        // fetch current user and insert into Room
        userNetworkDataSource.getUser(currentUser).toDomainModel().let {
            userDao.upsert(it)
        }

        // fetch current user's pairing and insert into Room
        // if partner is already added, fetch partner info and insert into Room
        pairingNetworkDataSource.getPairing(currentUser).toDomainModel(currentUser).let { pairing ->
            pairingDao.upsert(pairing)
            if (pairing.partnerId != null) {
                userNetworkDataSource.getUser(pairing.partnerId).toDomainModel().let {
                    userDao.upsert(it)
                }
            }
        }
    }
}