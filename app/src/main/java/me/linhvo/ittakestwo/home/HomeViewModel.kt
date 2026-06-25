package me.linhvo.ittakestwo.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.data.AuthRepository
import me.linhvo.ittakestwo.model.User

//sealed interface HomeUiState {
//    data class Success()
//}

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()


    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().onFailure {
                _errorMessage.value = it.message ?: ""
            }
        }
    }

    suspend fun getUserInfo(): User? {
        val user = viewModelScope.async {
            authRepository.getUser()
        }
        return user.await()
    }

    init {
        Log.d("view_model", "home view model started")

    }

    override fun onCleared() {
        super.onCleared()
        Log.d("view_model", "home view model cleared")
    }
}