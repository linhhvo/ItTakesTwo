package me.linhvo.ittakestwo.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.model.Pairing
import me.linhvo.ittakestwo.database.model.User
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
        pairingNetworkDataSource.addPartner(userId, partnerEmail).toDomainModel().let {
            userRepository.upsert(it)
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

    suspend fun syncPairingFromNetwork(currentUser: String) {
        pairingNetworkDataSource.getPairingStream(currentUser).collect { pairing ->
            pairing.toDomainModel(currentUser).let {
                pairingDao.upsert(it)
            }
        }
    }
}