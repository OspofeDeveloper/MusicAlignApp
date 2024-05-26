package com.example.musicalignapp.ui.screens.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.data.remote.core.NetError
import com.example.musicalignapp.data.remote.firebase.AuthService
import com.example.musicalignapp.domain.usecases.core.GetUserIdUseCase
import com.example.musicalignapp.domain.usecases.home.DeletePackageUseCase
import com.example.musicalignapp.domain.usecases.home.GetAllPackagesUseCase
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.uimodel.HomeUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPackagesUseCase: GetAllPackagesUseCase,
    private val deletePackageUseCase: DeletePackageUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val authService: AuthService
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<HomeUIModel>>(ScreenState.Loading())
    val uiState: StateFlow<ScreenState<HomeUIModel>> = _uiState

    init {
        getData()
    }

    fun getData() {
        getUserId()
    }

    private fun getUserId() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = getUserIdUseCase(USER_ID_KEY)
            if (userId.isNotBlank()) {
                getAllPackages(userId)
            } else {
                //Controlar error
            }
        }

    }

    private fun getAllPackages(userId: String) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()

            withContext(Dispatchers.IO) {
                getAllPackagesUseCase(userId).result(
                    ::onError, ::onSuccess
                )
            }
        }
    }

    fun deletePackage(
        packageId: String,
        fileId: String,
        imageId: String,
        jsonId: String,
        onPackageDeleted: () -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                deletePackageUseCase(packageId, fileId, imageId, jsonId)
            }
            if (result) {
                getData()
                onPackageDeleted()
            } else {
                //Handle Error
            }
        }
    }

    private fun onError(error: NetError) {
        _uiState.value = ScreenState.Error("error")
    }

    private fun onSuccess(data: HomeUIModel) {
        _uiState.value = ScreenState.Success(data)
    }

    fun logout(navigateToLogin: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            authService.logout()
            navigateToLogin()
        }
    }
}