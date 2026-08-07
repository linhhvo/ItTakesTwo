package me.linhvo.ittakestwo.network.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class StorageNetworkDataSource @Inject constructor(private val supabase: SupabaseClient) {

    suspend fun getDownloadUrl(bucketId: String, fileName: String?): String? {
        return if (fileName != null) {
            supabase.storage.from(bucketId).createSignedUrl(path = fileName, expiresIn = 1.hours)
        } else {
            null
        }
    }

    suspend fun uploadFile(bucketId: String, fileName: String?, byteArray: ByteArray?) {
        if (fileName != null) {
            val signedUrl = supabase.storage.from(bucketId).createSignedUploadUrl(fileName, upsert = true)

            byteArray?.let {
                supabase.storage.from(bucketId)
                    .uploadToSignedUrl(path = fileName, token = signedUrl.token, data = byteArray) {
                        upsert = true
                    }
            }
        }
    }
}