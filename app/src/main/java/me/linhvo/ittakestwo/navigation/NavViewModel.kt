package me.linhvo.ittakestwo.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.linhvo.ittakestwo.data.AuthRepository
import me.linhvo.ittakestwo.data.ChatRepository
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    val sessionStatus = authRepository.getSession()
}