package me.linhvo.ittakestwo.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import me.linhvo.ittakestwo.data.AuthRepository

class NavViewModel(
    private val savedStateHandle: SavedStateHandle,
) :
    ViewModel() {
    private val authRepository = AuthRepository()

    val sessionStatus = authRepository.getSession()
}