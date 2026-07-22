package me.linhvo.ittakestwo.repository

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import me.linhvo.ittakestwo.database.LocalDatabase
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.network.datasource.AuthNetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class AuthRepository @Inject constructor(
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val userDao: UserDao,
    private val localDatabase: LocalDatabase
) {
    val sessionStatus: StateFlow<SessionStatus> = authNetworkDataSource.getSession()

    val currentUserId: String
        get() = authNetworkDataSource.currentUserId ?: throw IllegalStateException("user ID is not available")

    suspend fun signUp(name: String, email: String, password: String) {
        authNetworkDataSource.signUp(name, email, password)

        val newUser = User(
            id = authNetworkDataSource.currentUserId
                ?: throw IllegalStateException("Cannot get current session user ID"),
            email = email,
            displayName = name,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        userDao.upsert(newUser)
    }

    suspend fun signIn(email: String, password: String) {
        authNetworkDataSource.signIn(email, password)
    }

    suspend fun signOut() {
        authNetworkDataSource.signOut()
        //TODO: maybe clear Room database
        localDatabase.clearAllTables()
    }
}