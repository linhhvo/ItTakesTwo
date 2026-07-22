package me.linhvo.ittakestwo.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val sessionStatus = authRepository.sessionStatus

    init {
        viewModelScope.launch {
            Log.d("debug_VM", "nav VM init")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("debug_VM", "nav VM clear")
    }
}