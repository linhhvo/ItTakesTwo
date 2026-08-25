package me.linhvo.ittakestwo.repository

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.network.datasource.StorageNetworkDataSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storageNetworkDataSource: StorageNetworkDataSource,
    @ApplicationContext private val appContext: Context,
) {
    val avatarDirPath: String
        get() = getDataDirPath(Environment.DIRECTORY_PICTURES) + appContext.resources.getString(R.string.avatar_dir)

    val messageAttachmentDirPath: String
        get() = getDataDirPath(Environment.DIRECTORY_PICTURES) + appContext.resources.getString(R.string.message_attachment_dir)

    fun getDataDirPath(type: String) =
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            appContext.getExternalFilesDir(type)?.path
        } else null

    suspend fun downloadFileFromNetwork(bucketId: String, fileName: String?): HttpResponse? =
        storageNetworkDataSource.getDownloadUrl(bucketId, fileName)?.let { url ->
            HttpClient().use { it.get(url) }
        }


    suspend fun saveFileToLocalStorage(dirPath: String?, fileName: String?, byteArray: ByteArray?) {
        withContext(Dispatchers.IO) {
            if (dirPath != null && fileName != null) {
                val file = File(dirPath, fileName)
                if (!File(dirPath).exists()) {
                    File(dirPath).mkdirs()
                }
                file.createNewFile()
                FileOutputStream(file, false).use { it.buffered().write(byteArray) }
            }
        }
    }

    suspend fun downloadAndSaveFile(bucketId: String, fileName: String?, dirPath: String?) {
        Log.d("debug_download", "downloading file $fileName")
        withContext(Dispatchers.IO) {
            if (dirPath != null && fileName != null) {
                val file = File(dirPath, fileName)
                if (!File(dirPath).exists()) {
                    File(dirPath).mkdirs()
                }
                file.createNewFile()

                storageNetworkDataSource.getDownloadUrl(bucketId, fileName)?.let { url ->
                    HttpClient().use { client ->
                        client.prepareGet(url).execute { res ->
                            FileOutputStream(file, false).use { out ->
                                res.bodyAsChannel().copyTo(out)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun saveAndUploadFile(bucketId: String, fileName: String?, dirPath: String?, byteArray: ByteArray?) {
        saveFileToLocalStorage(dirPath, fileName, byteArray)

        storageNetworkDataSource.uploadFile(bucketId, fileName, byteArray)
    }

    suspend fun saveToMediaStore(fileName: String, filePath: String) {
        withContext(Dispatchers.IO) {
            val resolver = appContext.contentResolver
            val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val file = File(filePath)

            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val imageUri = resolver.insert(collection, imageDetails)

            imageUri?.let {
                FileInputStream(file).use { inStream ->
                    resolver.openOutputStream(imageUri)?.use { outStream ->
                        inStream.copyTo(out = outStream)
                    }
                }
            }

            if (imageUri != null) {
                imageDetails.clear()
                imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, imageDetails, null, null)
            }
        }
    }
}