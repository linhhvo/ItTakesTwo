package me.linhvo.ittakestwo.repository

import android.content.Context
import android.os.Environment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.linhvo.ittakestwo.network.datasource.StorageNetworkDataSource
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storageNetworkDataSource: StorageNetworkDataSource
) {

    fun getDataDirPath(context: Context, type: String) =
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            context.getExternalFilesDir(type)?.path
        } else null

    suspend fun downloadFileFromNetwork(bucketId: String, fileName: String?): HttpResponse? =
        storageNetworkDataSource.getDownloadUrl(bucketId, fileName)?.let { url ->
            HttpClient().use { it.get(url) }
        }


    suspend fun saveFileToLocalStorage(dirPath: String?, fileName: String?, byteArray: ByteArray?) {
        withContext(Dispatchers.IO) {
            if (!Files.exists(Paths.get(dirPath))) {
                Files.createDirectory(Paths.get(dirPath))
            }
            fileName?.let {
                val file = File(dirPath, fileName)
                file.createNewFile()
                FileOutputStream(file, false).use { it.write(byteArray) }
            }
        }
    }

    suspend fun downloadAndSaveFile(bucketId: String, fileName: String?, dirPath: String?) {
        if (!Files.exists(Paths.get(dirPath, fileName))) {
            downloadFileFromNetwork(bucketId, fileName)?.let {
                saveFileToLocalStorage(dirPath, fileName, it.body<ByteArray>())
            }
        }
    }

    suspend fun saveAndUploadFile(bucketId: String, fileName: String?, dirPath: String?, byteArray: ByteArray?) {
        saveFileToLocalStorage(dirPath, fileName, byteArray)

        storageNetworkDataSource.uploadFile(bucketId, fileName, byteArray)
    }

}