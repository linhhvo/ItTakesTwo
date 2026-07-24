package me.linhvo.ittakestwo.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.model.Pairing
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.database.model.toNetworkModel
import me.linhvo.ittakestwo.network.datasource.PairingNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val pairingNetworkDataSource: PairingNetworkDataSource,
    private val pairingDao: PairingDao,
    private val userRepository: UserRepository
) {
    suspend fun addPartner(userId: String, partnerEmail: String) {
        pairingNetworkDataSource.addNewPairing(userId, partnerEmail).let { newPairing ->
            userRepository.upsertUserFromNetwork(userId = newPairing.partnerId)
            pairingDao.upsert(newPairing.toDomainModel(userId))
        }
    }

    fun getPairingStream(): Flow<Pairing?> = pairingDao.observePairing()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserPairStream(userId: String): Flow<Pair<User?, User?>> {
        val userFlow = userRepository.getUserStream(userId)
        val partnerFlow = getPairingStream().flatMapLatest {
            if (it?.partnerId != null) {
                userRepository.getUserStream(it.partnerId)
            } else {
                flowOf(null)
            }
        }

        return combine(userFlow, partnerFlow) { user, partner ->
            Pair(user, partner)
        }
    }

    suspend fun syncPairing(currentUser: String) {
        val localPairing = pairingDao.getPairing()
        val remotePairing = pairingNetworkDataSource.getPairing(currentUser)

        if (remotePairing == null && localPairing != null) {
            pairingNetworkDataSource.updatePairing(localPairing.toNetworkModel())
        } else if (localPairing == null && remotePairing != null) {
            pairingDao.upsert(remotePairing.toDomainModel(currentUser))
        } else if (localPairing != null && remotePairing != null) {
            if (localPairing != remotePairing.toDomainModel(currentUser)) {
                if (remotePairing.updatedAt < localPairing.updatedAt) {
                    pairingNetworkDataSource.updatePairing(localPairing.toNetworkModel())
                } else {
                    pairingDao.upsert(remotePairing.toDomainModel(currentUser))
                }
            }
        }

        pairingNetworkDataSource.getPairingStream(currentUser).collect { pairing ->
            val oldPairing = pairingDao.getPairing()
            pairing.toDomainModel(currentUser).let {
                if (oldPairing != null && it.id != oldPairing.id) {
                    pairingDao.delete(oldPairing)
                }

                if (oldPairing?.partnerId == null) {
                    userRepository.upsertUserFromNetwork(it.partnerId)
                }

                pairingDao.upsert(it)

            }
        }
    }
}