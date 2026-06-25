package me.linhvo.ittakestwo.navigation

import androidx.lifecycle.ViewModel
import me.linhvo.ittakestwo.data.AuthRepository

class NavViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    val sessionStatus = authRepository.getSession()
}