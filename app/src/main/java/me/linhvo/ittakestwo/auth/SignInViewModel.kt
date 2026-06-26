package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import me.linhvo.ittakestwo.data.AuthRepository

class SignInViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onSignInButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            authRepository.signIn(email = email.value, password = passwordStr).onFailure {
                _errorMessage.value = when (it) {
                    is AuthRestException -> {
                        it.errorDescription
                    }

                    is RestException -> {
                        it.description
                    }

                    is SerializationException -> {
                        "Display name is empty"
                    }

                    else -> {
                        it.toString()
                    }
                }
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    init {
        Log.d("view_model", "sign in VM started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "sign in VM cleared")
    }
}