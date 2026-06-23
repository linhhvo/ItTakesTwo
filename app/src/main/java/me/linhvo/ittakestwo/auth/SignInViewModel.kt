package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository

class SignInViewModel : ViewModel() {

    val authRepository = AuthRepository()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _success = MutableSharedFlow<Boolean>()
    val success: SharedFlow<Boolean>
        get() = _success.asSharedFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onSignInButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            val result = authRepository.signIn(email = email.value, password = passwordStr)
            _success.emit(result)
        }
    }

    init {
        Log.d("auth_signin", "sign in view model started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("auth_signin", "sign in view model cleared")
    }
}