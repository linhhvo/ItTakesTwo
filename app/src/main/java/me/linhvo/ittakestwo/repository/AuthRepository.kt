package me.linhvo.ittakestwo.repository

import com.google.firebase.Firebase
import com.google.firebase.installations.installations
import com.google.firebase.messaging.messaging
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.LocalDatabase
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.network.datasource.AuthNetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val localDatabase: LocalDatabase,
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val userRepository: UserRepository,
) {
    val sessionStatus: StateFlow<SessionStatus> = authNetworkDataSource.getSession()

    val currentUserId: String
        get() = authNetworkDataSource.currentUserId ?: throw IllegalStateException("user ID is not available")

    suspend fun signUp(name: String, email: String, password: String) {
        authNetworkDataSource.signUp(name, email, password)

        val newUser = User(
            id = currentUserId,
            email = email,
            displayName = name,
            updatedAt = authNetworkDataSource.currentUser?.updatedAt ?: Clock.System.now(),
            fid = Firebase.installations.id.await(),
            fcmToken = Firebase.messaging.token.await()
        )
        userRepository.addNewUser(newUser)
    }

    suspend fun signIn(email: String, password: String) {
        authNetworkDataSource.signIn(email, password)

        userRepository.updateUserFid(
            userId = currentUserId, fid = Firebase.installations.id.await(), fcmToken = Firebase.messaging.token.await()
        )
    }

    suspend fun signOut() {
        authNetworkDataSource.signOut()
        localDatabase.clearAllTables()
    }
}