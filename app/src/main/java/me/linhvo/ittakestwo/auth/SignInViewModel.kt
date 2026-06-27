package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import me.linhvo.ittakestwo.data.AuthRepository

data class SignInUiState(
    val email: String = "",
    val errorMessage: String? = null
)

class SignInViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(email = email)
        }
    }

    fun onSignInButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            authRepository.signIn(
                email = _uiState.value.email,
                password = passwordStr
            ).onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = when (e) {
                            is AuthRestException -> {
                                e.errorDescription
                            }

                            is RestException -> {
                                e.description
                            }

                            is SerializationException -> {
                                "Display name is empty"
                            }

                            else -> {
                                e.message.toString()
                            }
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
        Log.d("view_model", "sign in VM started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "sign in VM cleared")
    }
}