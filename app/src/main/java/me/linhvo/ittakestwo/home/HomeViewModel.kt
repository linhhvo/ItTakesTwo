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

data class HomeUiState(
    val userInitial: String = "",
//    val userAvatar: Int
    val partnerInitial: String = "",
    val errorMessage: String? = null,
)

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState: StateFlow<HomeUiState> =
        userRepository.getUserAndPartnerInfo()
            .combine(_errorMessage) { (user, partner), errorMessage ->
                HomeUiState(
                    userInitial = user?.displayName?.first()?.toString()?.uppercase() ?: "",
                    partnerInitial = partner?.displayName?.first()?.toString()?.uppercase() ?: "",
                    errorMessage = errorMessage
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

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    init {
        Log.d("view_model", "home view model started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "home view model cleared")
    }
}