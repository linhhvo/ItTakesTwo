package me.linhvo.ittakestwo.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.database.model.toNetworkModel
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val userDao: UserDao
) {
    suspend fun getUser(userId: String): User = userDao.loadUser(userId)

    fun addNewUser(user: User) {
        applicationScope.launch {
            userDao.upsert(user)
            userNetworkDataSource.upsertUser(user.toNetworkModel())
        }
    }

    suspend fun populateUserToLocalDatabase(userId: String) {
        val networkUser = userNetworkDataSource.getUser(userId)
        if (networkUser != null) {
            userDao.upsert(networkUser.toDomainModel())
        }
    }

    fun getUserStream(userId: String): Flow<User?> = userDao.observeUser(userId)

//    suspend fun uploadUserAvatar(context: Context, avatarUri) {
//    }

    suspend fun syncUsers(currentUser: String) {
        userDao.getUsers().forEach { localUser ->
            val remoteUser = userNetworkDataSource.getUser(localUser.id)

            if (remoteUser == null) {
                userNetworkDataSource.upsertUser(localUser.toNetworkModel())
            } else {
                if (localUser != remoteUser.toDomainModel()) {
                    if (remoteUser.updatedAt < localUser.updatedAt) {
                        userNetworkDataSource.upsertUser(localUser.toNetworkModel())
                    } else {
                        userDao.upsert(remoteUser.toDomainModel())
                    }
                }
            }
        }
        userNetworkDataSource.getUserStream(currentUser).collect {
            userDao.upsert(it.toDomainModel())
        }
    }
}