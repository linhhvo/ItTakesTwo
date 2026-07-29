package me.linhvo.ittakestwo.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.datastore.ConfigsDataSource
import me.linhvo.ittakestwo.repository.AuthRepository
import me.linhvo.ittakestwo.repository.ChatRepository
import me.linhvo.ittakestwo.repository.PairingRepository
import javax.inject.Inject

data class ChatUiState(
    val user: User? = null,
    val partner: User? = null,
    val chatMessages: List<Message> = emptyList(),
    val userInput: String = "",
    val errorMessage: String? = null,
    val showNotiPermissionRequest: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val pairingRepository: PairingRepository,
    private val chatRepository: ChatRepository,
    private val configs: ConfigsDataSource
) : ViewModel() {
    private val _currentUserId = authRepository.currentUserId
    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _userInput = MutableStateFlow("")
    private val _showPermissionRequest = MutableStateFlow(false)

    val uiState: StateFlow<ChatUiState> =
        combine(
            pairingRepository.getUserPairStream(_currentUserId),
            chatRepository.getMessageListStream(),
            _errorMessage,
            _userInput,
            _showPermissionRequest
        ) { userPair, messages, errorMessage, userInput, showPermissionRequest ->
            ChatUiState(
                user = userPair.first,
                partner = userPair.second,
                chatMessages = messages,
                userInput = userInput,
                errorMessage = errorMessage,
                showNotiPermissionRequest = showPermissionRequest
            )
        }.catch {
            emit(ChatUiState(errorMessage = it.message))
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = ChatUiState()
        )

    fun onInputChange(input: String) {
        _userInput.value = input
    }

    fun sendMessage() {
        viewModelScope.launch {
            val cleanInput = _userInput.value.trim()
            if (cleanInput.isNotBlank()) {
                try {
                    chatRepository.addNewMessage(
                        senderId = _currentUserId,
                        recipientId = pairingRepository.getPartnerId()
                            ?: throw IllegalStateException("Partner is not available"),
                        content = cleanInput
                    )
                    _userInput.value = ""
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            }
        }
    }

    fun updateNotiPermissionResponse() {
        _showPermissionRequest.value = false
        viewModelScope.launch {
            configs.updateNotiPermissionResponse(true)
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun markAsRead() {
        viewModelScope.launch {
            chatRepository.markAllAsRead(_currentUserId)
        }
    }

    init {
        Log.d("debug_VM", "chat VM init")
        viewModelScope.launch {
            _showPermissionRequest.value = !configs.getInitialConfigs().notiPermissionResponded
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "chat VM clear")
    }
}