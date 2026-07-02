package me.linhvo.ittakestwo.home

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
//    val userInitial: String = "",
//    val userAvatar: Int
//    val partnerInitial: String = "",
    val user: User? = null,
    val partner: User? = null,
    val errorMessage: String? = null,
    val shouldShowProfile: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository(viewModelScope)

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _shouldShowProfile = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> =
        combine(userRepository.getUserAndPartnerInfo(), _errorMessage, _shouldShowProfile)
        { (user, partner), errorMessage, shouldShowProfile ->
            HomeUiState(
//                    userInitial = user?.displayName?.first()?.toString()?.uppercase() ?: "",
//                    partnerInitial = partner?.displayName?.first()?.toString()?.uppercase() ?: "",
                user = user,
                partner = partner,
                errorMessage = errorMessage,
                shouldShowProfile = shouldShowProfile
            )
        }.catch {
            Log.d("debugging", it.message.toString())
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