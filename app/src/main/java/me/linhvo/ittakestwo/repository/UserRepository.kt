package me.linhvo.ittakestwo.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.database.model.toNetworkModel
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class UserRepository @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val appContext: Context,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val userDao: UserDao
) {
    private val avatarDir = "/profile_avatars"

    fun addNewUser(user: User) {
        appScope.launch {
            userDao.upsert(user)
            userNetworkDataSource.upsertUser(user.toNetworkModel())
        }
    }

    suspend fun updateUserFid(userId: String, fid: String, fcmToken: String) {
        userNetworkDataSource.updateUserFid(userId, fid, fcmToken)
    }

    suspend fun populateUserToLocalDatabase(userId: String) {
        val networkUser = userNetworkDataSource.getUser(userId)

        if (networkUser != null) {
            val user = networkUser.toDomainModel()

            if (user.avatarFile != null) {
                val dirPath = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + avatarDir

                if (!Files.exists(Paths.get(dirPath, user.avatarFile))) {
                    val downloadUrl = userNetworkDataSource.getAvatarUrlFromNet(user.avatarFile)
                    val response = HttpClient().use { it.get(downloadUrl!!) }

                    withContext(Dispatchers.IO) {
                        Files.createDirectory(Paths.get(dirPath))
                        val file = File(dirPath, user.avatarFile!!)
                        file.createNewFile()
                        FileOutputStream(file, false).use { it.write(response.body<ByteArray>()) }
                    }
                }
                user.avatarPath = dirPath + "/" + user.avatarFile
            }

            userDao.upsert(user)
        }
    }

    suspend fun updateUserAvatar(userId: String, avatarUri: Uri) {
        val user = userDao.loadUser(userId)

        val avatarVersion = user.avatarFile?.removeSuffix(".png")?.last()?.digitToInt()?.inc() ?: 1
        user.avatarFile = "${userId}_v${avatarVersion}.png"

        val byteArray =
            appContext.contentResolver.openInputStream(avatarUri)?.use { it.buffered().readBytes() }

        // check if external storage is writable
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dirPath = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + avatarDir
            val file = File(dirPath, user.avatarFile!!)

            withContext(Dispatchers.IO) {
                if (!Files.exists(Paths.get(dirPath))) {
                    Files.createDirectory(Paths.get(dirPath))
                }

                file.createNewFile()
                FileOutputStream(file, false).use { it.write(byteArray) }
            }

            user.avatarPath = dirPath + "/" + user.avatarFile
        }

        user.updatedAt = Clock.System.now()
        userNetworkDataSource.upsertUser(user.toNetworkModel())
        userDao.upsert(user)

        userNetworkDataSource.uploadUserAvatar(
            fileName = user.avatarFile!!,
            byteArray = byteArray
        )
    }

    fun getUserStream(userId: String): Flow<User?> = userDao.observeUser(userId)

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
            //TODO: convert avatarfile to avatarpath here
            userDao.upsert(it.toDomainModel())
        }
    }
}