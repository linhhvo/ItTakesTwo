package me.linhvo.ittakestwo.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.linhvo.ittakestwo.Configs
import me.linhvo.ittakestwo.copy
import javax.inject.Inject

class ConfigsDataSource @Inject constructor(
    private val configs: DataStore<Configs>
) {
    fun hasNotiPermissionResponded(): Flow<Boolean> =
        configs.data.map { it.notiPermissionResponded }

    suspend fun updateNotiPermissionResponse(value: Boolean) {
        configs.updateData { it.copy { notiPermissionResponded = value } }
    }

    suspend fun getInitialConfigs() =
        configs.data.first()
}