package me.linhvo.ittakestwo.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import me.linhvo.ittakestwo.data.PairingRepository
import me.linhvo.ittakestwo.data.UserRepository
import me.linhvo.ittakestwo.model.User

data class ChatUiState(
    val user: User? = null,
    val partner: User? = null,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val pairingRepository = PairingRepository()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState: StateFlow<ChatUiState> =
        combine(pairingRepository.getProfileStream(), _errorMessage) { (user, partner), error ->
            ChatUiState(
                user = user,
                partner = partner,
                errorMessage = error
            )
        }.catch {
            emit(ChatUiState(errorMessage = it.message))
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = ChatUiState()
        )

    init {
        Log.d("debug_VM", "chat VM init")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "chat VM clear")
    }
}