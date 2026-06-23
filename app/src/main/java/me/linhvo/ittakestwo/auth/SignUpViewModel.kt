package me.linhvo.ittakestwo.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository

class SignUpViewModel : ViewModel() {

    val authRepository = AuthRepository()
    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _success = MutableSharedFlow<Boolean>()
    val success: SharedFlow<Boolean>
        get() = _success.asSharedFlow()

    fun onDisplayNameChange(displayName: String) {
        _displayName.value = displayName
    }

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onSignUpButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            val result = authRepository.signUp(name = displayName.value, email = email.value, password = passwordStr)
            _success.emit(result)
        }
    }
}