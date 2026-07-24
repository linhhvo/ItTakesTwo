package me.linhvo.ittakestwo.database

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.repository.ChatRepository
import me.linhvo.ittakestwo.repository.PairingRepository
import me.linhvo.ittakestwo.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncRepository @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
//    private val userDao: UserDao,
//    private val userNetworkDataSource: UserNetworkDataSource,
//    private val pairingNetworkDataSource: PairingNetworkDataSource,
//    private val pairingDao: PairingDao,
    private val userRepository: UserRepository,
    private val pairingRepository: PairingRepository,
    private val chatRepository: ChatRepository
) {
    fun initializeData(currentUser: String) {
        applicationScope.launch {
            try {
                // fetch current user and insert into Room
//                userNetworkDataSource.getUser(currentUser)?.toDomainModel()?.let {
//                    userDao.upsert(it)
//                }
                userRepository.populateUserToLocalDatabase(currentUser)

                // fetch current user's pairing and insert into Room
                // if partner is already added, fetch partner info and insert into Room
//                pairingNetworkDataSource.getPairing(currentUser)?.toDomainModel(currentUser)?.let { pairing ->
//                    pairingDao.upsert(pairing)
//                    userNetworkDataSource.getUser(pairing.partnerId)?.toDomainModel()?.let {
//                        userDao.upsert(it)
//                    }
//                }
                pairingRepository.populatePairingToLocalDatabase(currentUser)

                // fetch and insert messages
                chatRepository.populateMessagesToLocalDatabase(currentUser)

            } catch (e: Exception) {
                Log.d("debug_initializeData_Error", e.toString())
            }
        }
    }
}