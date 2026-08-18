package me.linhvo.ittakestwo.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.repository.ChatRepository
import me.linhvo.ittakestwo.repository.UserRepository
import javax.inject.Inject

data class MediaUiState(
    val attachment: Attachment? = null,
    val senderName: String? = null,
    val shouldShowOverlay: Boolean = true
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _attachment = MutableStateFlow<Attachment?>(null)
    private val _senderName = MutableStateFlow<String?>(null)
    private val _shouldShowOverlay = MutableStateFlow(true)

    val uiState: StateFlow<MediaUiState> =
        combine(_attachment, _shouldShowOverlay, _senderName) { attachment, shouldShowOverlay, senderName ->
            MediaUiState(
                attachment = attachment,
                senderName = senderName,
                shouldShowOverlay = shouldShowOverlay
            )
        }.catch {
            emit(MediaUiState())
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = MediaUiState()
        )

    fun getAttachment(attachmentId: String) {
        viewModelScope.launch {
            _attachment.value = chatRepository.getAttachmentById(attachmentId)?.also {
                chatRepository.getMessageById(it.messageId).let { message ->
                    _senderName.value = userRepository.getUser(message.senderId)?.displayName
                }
            }
        }
    }

    fun toggleOverlay() {
        _shouldShowOverlay.value = !_shouldShowOverlay.value
    }
}