package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import me.linhvo.ittakestwo.repository.AuthRepository
import javax.inject.Inject

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onSignUpButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            try {
                authRepository.signUp(
                    name = _uiState.value.displayName,
                    email = _uiState.value.email,
                    password = passwordStr
                )
            } catch (e: Exception) {
                Log.d("debug_signup", e.toString())
                _uiState.update { state ->
                    state.copy(
                        errorMessage = when (e) {
                            is AuthRestException -> e.errorDescription
                            is RestException -> e.description
                            is SerializationException -> "Display name is empty"
                            else -> e.message
                        }
                    )
                }
            }
        }
    }

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    init {
        Log.d("view_model", "sign up vm started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "sign up vm cleared")
    }
}