package me.linhvo.ittakestwo.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.User
import me.linhvo.ittakestwo.repository.AuthRepository
import me.linhvo.ittakestwo.repository.PairingRepository
import me.linhvo.ittakestwo.repository.UserRepository
import javax.inject.Inject

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
//            pairingNetworkDataSource.getProfileStream(_currentUserId!!),
            userRepository.getUserStream(_currentUserId),
            _errorMessage,
            _shouldShowProfile,
            _backgroundImage
        )
        { /*(user, partner)*/ user, errorMessage, shouldShowProfile, backgroundImage ->
            HomeUiState(
                user = user,
//                partner = partner,
//                partner = null,
                backgroundImageUrl = backgroundImage,
                errorMessage = errorMessage,
                shouldShowProfile = shouldShowProfile
            )
//        }.catch {
//            emit(HomeUiState(errorMessage = it.message))
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
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
//                pairingNetworkDataSource.addPartner(userId = _currentUserId!!, partnerEmail = partnerEmail)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun uploadAvatar(context: Context, avatarUri: Uri) {
        viewModelScope.launch {
            try {
//                userRepository.uploadUserAvatar(context, avatarUri)
            } catch (e: Exception) {
                if (e !is IllegalStateException) {
                    _errorMessage.value = e.message
                } else {
                    Log.d("debug_uploadAvatar", e.message.toString())
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
            userRepository.syncUsers(authRepository.currentUserId)
            pairingRepository.syncPairing(authRepository.currentUserId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "home VM clear")
    }
}