package me.linhvo.ittakestwo.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.repository.ChatRepository
import me.linhvo.ittakestwo.repository.StorageRepository
import me.linhvo.ittakestwo.repository.UserRepository
import javax.inject.Inject

data class MediaUiState(
    val attachmentList: List<Attachment> = emptyList(),
    val targetAttachment: Attachment? = null,
    val senderName: String? = null,
    val shouldShowOverlay: Boolean = true,
    val statusMessage: String? = null
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _targetAttachment = MutableStateFlow<Attachment?>(null)
    private val _senderName = MutableStateFlow<String?>(null)
    private val _shouldShowOverlay = MutableStateFlow(true)
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MediaUiState> =
        combine(
            chatRepository.getAttachmentListStream(),
            _targetAttachment,
            _shouldShowOverlay,
            _senderName,
            _statusMessage
        ) { attachmentList, targetAttachment, shouldShowOverlay, senderName, statusMessage ->
            MediaUiState(
                attachmentList = attachmentList,
                targetAttachment = targetAttachment,
                senderName = senderName,
                shouldShowOverlay = shouldShowOverlay,
                statusMessage = statusMessage
            )
        }.catch {
            emit(MediaUiState(statusMessage = "Cannot initialize UI state for media viewer"))
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = MediaUiState()
        )

    fun loadAttachment(attachment: Attachment) {
        viewModelScope.launch {
            _targetAttachment.value = attachment
            chatRepository.getMessageById(attachment.messageId).let { message ->
                _senderName.value = userRepository.getUser(message.senderId)?.displayName
            }
        }
    }

    fun toggleOverlay() {
        _shouldShowOverlay.value = !_shouldShowOverlay.value
    }

    fun saveMedia(fileName: String?, filePath: String?) {
        _statusMessage.value = "Downloading file..."
        if (fileName != null && filePath != null)
            viewModelScope.launch {
                try {
                    storageRepository.saveToMediaStore(fileName, filePath)
                    _statusMessage.value = "Downloaded"
                } catch (e: Exception) {
                    _statusMessage.value = "Download failed"
                }
            }
    }

}