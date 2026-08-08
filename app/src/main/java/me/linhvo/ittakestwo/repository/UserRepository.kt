package me.linhvo.ittakestwo.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.common.ApplicationScope
import me.linhvo.ittakestwo.database.dao.UserDao
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.database.model.toNetworkModel
import me.linhvo.ittakestwo.network.datasource.UserNetworkDataSource
import me.linhvo.ittakestwo.network.model.toDomainModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class UserRepository @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val appContext: Context,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val userDao: UserDao,
    private val storageRepository: StorageRepository
) {
    private val avatarDir = appContext.resources.getString(R.string.avatar_dir)
    private val imageSuffix = appContext.resources.getString(R.string.image_file_suffix)
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
                val dirPath = storageRepository.getDataDirPath(appContext, Environment.DIRECTORY_PICTURES)?.let {
                    it + avatarDir
                }
                storageRepository.downloadAndSaveFile(
                    bucketId = "avatars",
                    fileName = user.avatarFile,
                    dirPath = dirPath
                )
                user.avatarPath = dirPath + "/" + user.avatarFile
            }
            userDao.upsert(user)
        }
    }

    suspend fun updateUserAvatar(userId: String, avatarUri: Uri) {
        val user = userDao.loadUser(userId)

        if (user != null) {
            val avatarVersion =
                user.avatarFile?.removeSuffix(imageSuffix)?.substringAfterLast("_")
                    ?.toIntOrNull()?.inc() ?: 1
            user.avatarFile = "${userId}_${avatarVersion}$imageSuffix"

            val byteArray =
                appContext.contentResolver.openInputStream(avatarUri)?.use { it.buffered().readBytes() }

            val dirPath =
                storageRepository.getDataDirPath(appContext, Environment.DIRECTORY_PICTURES)?.let { it + avatarDir }

            user.avatarPath = dirPath + "/" + user.avatarFile

            try {
                storageRepository.saveAndUploadFile(
                    bucketId = "avatars",
                    fileName = user.avatarFile,
                    dirPath = dirPath,
                    byteArray = byteArray
                )
            } finally {
                user.updatedAt = Clock.System.now()
                userNetworkDataSource.upsertUser(user.toNetworkModel())
                userDao.upsert(user)
            }
        }
    }

    fun getUserStream(userId: String): Flow<User?> = userDao.observeUser(userId)

    suspend fun syncUsers(currentUser: String) {
        userDao.getUsers().forEach { localUser ->
            val networkUser = userNetworkDataSource.getUser(localUser.id)

            if (networkUser == null) {
                userNetworkDataSource.upsertUser(localUser.toNetworkModel())
            } else {
                if (localUser != networkUser.toDomainModel()) {
                    if (networkUser.updatedAt < localUser.updatedAt) {
                        userNetworkDataSource.upsertUser(localUser.toNetworkModel())
                    } else {
                        val user = networkUser.toDomainModel()

                        val dirPath =
                            storageRepository.getDataDirPath(appContext, Environment.DIRECTORY_PICTURES)?.let {
                                it + avatarDir
                            }
                        if (user.avatarFile != localUser.avatarFile) {
                            storageRepository.downloadAndSaveFile(
                                bucketId = "avatars",
                                fileName = user.avatarFile,
                                dirPath = dirPath
                            )
                        }
                        user.avatarPath = dirPath + "/" + user.avatarFile
                        userDao.upsert(user)
                    }
                }
            }
        }
        userNetworkDataSource.getUserStream(currentUser).collect {
            val user = it.toDomainModel()
            user.avatarPath =
                storageRepository.getDataDirPath(
                    appContext,
                    Environment.DIRECTORY_PICTURES
                ) + avatarDir + "/" + user.avatarFile

            val dirPath =
                storageRepository.getDataDirPath(appContext, Environment.DIRECTORY_PICTURES)?.let { path ->
                    path + avatarDir
                }

            val oldUser = userDao.loadUser(user.id)

            if (user.avatarPath != oldUser?.avatarPath) {
                storageRepository.downloadAndSaveFile(
                    bucketId = "avatars",
                    fileName = user.avatarFile,
                    dirPath = dirPath
                )
            }
            userDao.upsert(user)
        }
    }
}