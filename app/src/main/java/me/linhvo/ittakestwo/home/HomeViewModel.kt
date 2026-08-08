package me.linhvo.ittakestwo.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.repository.AuthRepository
import me.linhvo.ittakestwo.repository.PairingRepository
import me.linhvo.ittakestwo.repository.UserRepository
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

data class HomeUiState(
    val user: User? = null,
    val partner: User? = null,
    val backgroundImageUrl: String? = null,
    val errorMessage: String? = null,
    val shouldShowProfile: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val pairingRepository: PairingRepository
) : ViewModel() {
    private val _currentUserId = authRepository.currentUserId
    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _shouldShowProfile = MutableStateFlow(false)
    private val _backgroundImage: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            pairingRepository.getUserPairStream(_currentUserId),
            _errorMessage,
            _shouldShowProfile,
            _backgroundImage
        )
        { userPair, errorMessage, shouldShowProfile, backgroundImage ->
            HomeUiState(
                user = userPair.first,
                partner = userPair.second,
                backgroundImageUrl = backgroundImage,
                errorMessage = errorMessage,
                shouldShowProfile = shouldShowProfile
            )
        }.catch {
            emit(HomeUiState(errorMessage = it.message))
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    @OptIn(ExperimentalPathApi::class)
    fun signOut(context: Context) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                context.getExternalFilesDir(null)?.toPath()?.deleteRecursively()
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is AuthRestException -> e.errorDescription
                    is RestException -> e.description
                    else -> e.message
                }.toString()
            }
        }
    }

    fun addPartner(partnerEmail: String) {
        viewModelScope.launch {
            try {
                pairingRepository.addPartner(userId = _currentUserId, partnerEmail = partnerEmail)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun uploadAvatar(avatarUri: Uri?) {
        if (avatarUri != null) {
            viewModelScope.launch {
                try {
                    userRepository.updateUserAvatar(userId = _currentUserId, avatarUri = avatarUri)
                } catch (e: Exception) {
                    if (e !is IllegalStateException) {
                        _errorMessage.value = e.message
                        Log.d("debug_uploadAvatar_Error", e.toString())
                    } else {
                        Log.d("debug_uploadAvatar_Error", e.toString())
                    }
                }
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun openProfile() {
        _shouldShowProfile.value = true
    }

    fun closeProfile() {
        _shouldShowProfile.value = false
    }

//    fun getBackground() {
//        Log.d("debug_background", "get background...")
//        viewModelScope.launch {
//            _backgroundImage.value = pairingRepository.getBackgroundUrlFromNet()
//        }
//    }

    init {
        Log.d("debug_VM", "home VM init")

        viewModelScope.launch {
            try {
                userRepository.syncUsers(authRepository.currentUserId)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _errorMessage.value = "Error syncing with database to get user info. Restart the app"
                }
            }
        }
        viewModelScope.launch {
            try {
                pairingRepository.syncPairing(authRepository.currentUserId)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _errorMessage.value = "Error syncing with database to get pairing info. Restart the app"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "home VM clear")
    }
}