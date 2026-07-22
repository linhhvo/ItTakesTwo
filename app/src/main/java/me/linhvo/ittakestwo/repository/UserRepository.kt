package me.linhvo.ittakestwo.repository

import android.util.Log
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

    fun getUserStream(userId: String): Flow<User?> = userDao.observeUser(userId)

//    suspend fun uploadUserAvatar(context: Context, avatarUri) {
//    }

    suspend fun syncUsers(currentUser: String) {
        userNetworkDataSource.getUserStream(currentUser).collect {
            Log.d("debug_user", it.toString())
            userDao.upsert(it.toDomainModel())
        }
    }
}