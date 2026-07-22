package me.linhvo.ittakestwo.repository

import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userNetworkDataSource: UserNetworkDataSource,
    private val userDao: UserDao
) {
    suspend fun getUser(userId: String): User = userDao.loadUser(userId)

    suspend fun upsert(user: User) = userDao.upsert(user)

    fun getUserStream(userId: String): Flow<User?> = userDao.observeUser(userId)

//    suspend fun uploadUserAvatar(context: Context, avatarUri) {
//    }

    suspend fun syncUsersFromNetwork(currentUser: String) {
        userNetworkDataSource.getUserStream(currentUser).collect {
            userDao.upsert(it.toDomainModel())
        }
    }
}