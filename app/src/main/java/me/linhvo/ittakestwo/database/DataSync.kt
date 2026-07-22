package me.linhvo.ittakestwo.database

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.network.datasource.PairingNetworkDataSource
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncRepository @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val userDao: UserDao,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val pairingNetworkDataSource: PairingNetworkDataSource,
    private val pairingDao: PairingDao
) {
    fun initializeData(currentUser: String) {
        applicationScope.launch {
            try {
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
            } catch (e: Exception) {
                Log.d("debug_initializeData", e.message.toString())
            }
        }
    }
}