package me.linhvo.ittakestwo.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository
import me.linhvo.ittakestwo.data.UserRepository
import me.linhvo.ittakestwo.model.User

data class HomeUiState(
    val user: User? = null,
    val partner: User? = null,
    val errorMessage: String? = null,
    val shouldShowProfile: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _shouldShowProfile = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> =
        combine(
            userRepository.getProfileStream(),
            _errorMessage,
            _shouldShowProfile
        )
        { (user, partner), errorMessage, shouldShowProfile ->
            HomeUiState(
                user = user,
                partner = partner,
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
                userRepository.addPartner(partnerEmail)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun uploadAvatar(context: Context, avatarUri: Uri) {
        viewModelScope.launch {
            try {
                userRepository.uploadUserAvatar(context, avatarUri)
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

}