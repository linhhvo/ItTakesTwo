package me.linhvo.ittakestwo.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.DataSyncRepository
import me.linhvo.ittakestwo.repository.AuthRepository
import me.linhvo.ittakestwo.repository.ChatRepository
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val dataSync: DataSyncRepository
) : ViewModel() {
    val sessionStatus = authRepository.sessionStatus

    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount = _unreadMessageCount.asStateFlow()
    fun initializeData() {
        viewModelScope.launch {
            dataSync.initializeData(authRepository.currentUserId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getUnreadCount() {
        chatRepository.getMessageListStream()
            .mapLatest { messages ->
                messages.filter { !it.key.isSenderMe && it.key.readAt == null }.size
            }
            .distinctUntilChanged()
            .collect {
                _unreadMessageCount.value = it
            }
    }

    init {
        Log.d("debug_VM", "nav VM init")
        viewModelScope.launch {
            getUnreadCount()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "nav VM clear")
    }
}