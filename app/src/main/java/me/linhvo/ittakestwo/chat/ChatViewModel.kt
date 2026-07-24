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
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pairingRepository: PairingRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _currentUserId = authRepository.currentUserId
    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _userInput = MutableStateFlow("")

    val uiState: StateFlow<ChatUiState> =
        combine(
            pairingRepository.getUserPairStream(_currentUserId),
            chatRepository.getMessageListStream(),
            _errorMessage,
            _userInput
        ) { userPair, messages, errorMessage, userInput ->
            ChatUiState(
                user = userPair.first,
                partner = userPair.second,
                chatMessages = messages,
                userInput = userInput,
                errorMessage = errorMessage
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
                            ?: throw IllegalStateException("partner ID is not available"),
                        content = cleanInput
                    )
                    _userInput.value = ""
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                }
            }
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
            chatRepository.populateMessagesToLocalDatabase(_currentUserId)
            Log.d("debug_messages", "populated messages")
        }
        viewModelScope.launch {
            Log.d("debug_messages", "syncing messages")
            chatRepository.syncMessages(_currentUserId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "chat VM clear")
    }
}