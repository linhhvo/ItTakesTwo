package me.linhvo.ittakestwo.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.network.datasource.MessageNetworkDataSource
import me.linhvo.ittakestwo.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class NavBarViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val messageNetworkDataSource: MessageNetworkDataSource
) :
    ViewModel() {
    private val _currentUserId = authRepository.currentUserId
    private val _unreadMessageCount = MutableStateFlow<Long>(0)
    val unreadMessageCount = _unreadMessageCount.asStateFlow()

    fun getUnreadCount() {
        viewModelScope.launch {
            _unreadMessageCount.value = messageNetworkDataSource.getUnreadCount(_currentUserId!!)
            Log.d("debug_unreadCount", "initial count: ${_unreadMessageCount.value}")
        }
    }

    init {
        Log.d("debug_VM", "Nav Bar VM init")

        viewModelScope.launch {
            messageNetworkDataSource.getMessageStream(_currentUserId!!).collect {
                Log.d("debug_unreadCount", "new message")
                _unreadMessageCount.value++
                Log.d("debug_unreadCount", "new count: ${_unreadMessageCount.value}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "nav bar VM cleared")
    }
}