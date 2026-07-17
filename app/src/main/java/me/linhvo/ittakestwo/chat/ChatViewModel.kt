package me.linhvo.ittakestwo.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.ChatRepository
import me.linhvo.ittakestwo.data.PairingRepository
import me.linhvo.ittakestwo.model.Message
import me.linhvo.ittakestwo.model.User

data class ChatUiState(
    val user: User? = null,
    val partner: User? = null,
    val errorMessage: String? = null,
    val chatMessages: List<Message> = emptyList(),
    val userInput: String = ""
)

class ChatViewModel : ViewModel() {
    private val pairingRepository = PairingRepository()
    private val chatRepository = ChatRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    fun onInputChange(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val cleanInput = _uiState.value.userInput.trim()
            if (cleanInput.isNotEmpty()) {
                try {
                    chatRepository.addMessage(cleanInput)
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
        _uiState.update { it.copy(userInput = "") }
    }

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun markAsRead() {
        viewModelScope.launch {
            chatRepository.updateReadTime()
        }
    }

    init {
        Log.d("debug_VM", "chat VM init")
        viewModelScope.launch {
            val users = pairingRepository.getProfileInfo()
            val messages = chatRepository.getMessages()

            _uiState.update { it.copy(user = users.first, partner = users.second, chatMessages = messages) }
        }

        viewModelScope.launch {
            chatRepository.getMessageStream().collect { message ->
//                Log.d("debug_newMessage", message.toString())
                val newList = _uiState.value.chatMessages.toMutableList()
                val existingMessageInd = newList.indexOfFirst { it.id == message.id }

                if (existingMessageInd != -1) {
                    newList.removeAt(existingMessageInd)
                    newList.add(existingMessageInd, message)
                } else {
                    newList.add(0, message)
                }

                _uiState.update { it.copy(chatMessages = newList) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "chat VM clear")
    }
}