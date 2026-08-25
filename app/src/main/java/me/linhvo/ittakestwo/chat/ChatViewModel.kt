package me.linhvo.ittakestwo.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.Attachment
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
    val chatMessages: Map<Message, List<Attachment>?> = emptyMap(),
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
    private val _selectedFiles = MutableStateFlow<MutableMap<Uri, ByteArray?>>(mutableMapOf())

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

    fun onFileSelection(context: Context, fileUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            fileUris.forEach { uri ->
                _selectedFiles.value[uri] =
                    context.contentResolver.openInputStream(uri)?.use { it.buffered().readBytes() }
            }
        }
    }

    fun onFileDeselection(fileUris: List<Uri>) {
        fileUris.forEach { _selectedFiles.value.remove(it) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val cleanInput = _userInput.value.trim()
            try {
                chatRepository.addNewMessage(
                    senderId = _currentUserId,
                    recipientId = pairingRepository.getPartnerId()
                        ?: throw IllegalStateException("Partner is not available"),
                    content = cleanInput,
                    byteArrays = _selectedFiles.value.values.toSet()
                )
                _userInput.value = ""
                _selectedFiles.value.clear()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun downloadAttachments(attachments: List<Attachment>?) {
        viewModelScope.launch {
            chatRepository.downloadAttachments(attachments)
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

    fun matchUrl(content: String): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()

        Patterns.WEB_URL.matcher(content).results().forEach {
            results += Pair(it.start(), it.end())
        }
        return results
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