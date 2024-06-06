package com.example.musicalignapp.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.data.remote.firebase.AuthService
import com.example.musicalignapp.domain.usecases.core.GetUserIdUseCase
import com.example.musicalignapp.domain.usecases.login.SaveUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val saveUserIdUseCase: SaveUserIdUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(
        user: String,
        password: String,
        navigateToHome: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = withContext(Dispatchers.IO) {
                    authService.login(user, password)
                }

                if (result != null) {
                    val isSaved = saveUserIdUseCase(USER_ID_KEY, result.uid)
                    if (isSaved) {
                        navigateToHome()
                    } else {
                        onError("Hubo un problema, intentelo mas tarde")
                    }
                }
            } catch (e: Exception) {
                onError(e.message.toString())
            }

            _isLoading.value = false
        }
    }
}