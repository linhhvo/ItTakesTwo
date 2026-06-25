package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository

class SignInViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onSignInButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            authRepository.signIn(email = email.value, password = passwordStr).onFailure {
                _errorMessage.value = it.message ?: ""
                Log.d("auth", _errorMessage.value)
            }
        }
    }

    init {
        Log.d("view_model", "sign in VM started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "sign in VM cleared")
    }
}