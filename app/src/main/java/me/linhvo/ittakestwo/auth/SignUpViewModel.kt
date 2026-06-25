package me.linhvo.ittakestwo.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _signUpState = MutableStateFlow(false)
    val signUpState = _signUpState.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    fun onDisplayNameChange(displayName: String) {
        _displayName.value = displayName
    }

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onSignUpButtonClick(password: CharSequence) {
        val passwordStr = password.toString()
        viewModelScope.launch {
            authRepository.signUp(name = displayName.value, email = email.value, password = passwordStr).onSuccess {
                _signUpState.value = true
            }.onFailure {
                _signUpState.value = false
                _errorMessage.value = it.message ?: ""
            }
        }
    }

    init {
        Log.d("view_model", "sign up vm started")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "sign up vm cleared")
    }
}