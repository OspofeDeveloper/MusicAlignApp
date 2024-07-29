package com.example.musicalignapp.ui.screens.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.data.remote.firebase.AuthService
import com.example.musicalignapp.domain.usecases.login.SaveUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authService: AuthService,
    private val saveUserIdUseCase: SaveUserIdUseCase,
): ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = withContext(Dispatchers.IO) {
                    authService.register(email, password)
                }

                if (result != null) {
                    saveUserIdUseCase(Constants.USER_ID_KEY, result.uid, Constants.USER_EMAIL_KEY, email)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.d("Error", "Register error: ${e.message}")
            }
            _isLoading.value = false
        }
    }
}
