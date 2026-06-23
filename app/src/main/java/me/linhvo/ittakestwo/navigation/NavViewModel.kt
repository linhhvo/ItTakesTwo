package me.linhvo.ittakestwo.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavViewModel : ViewModel() {
    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn = _isSignedIn.asStateFlow()

    fun userSignIn() {
        _isSignedIn.value = true
    }

    fun useSignOut() {
        _isSignedIn.value = false
    }
}