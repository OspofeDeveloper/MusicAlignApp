package com.example.musicalignapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.domain.usecases.home.DeletePackageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DataBaseService,
    private val deletePackageUseCase: DeletePackageUseCase
) : ViewModel() {

    private var _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState

    init {
        getData()
    }

    fun getData() {
        gatAllPackages()
    }

    private fun gatAllPackages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val response = withContext(Dispatchers.IO) {
                repository.getAllPackages()
            }
            _uiState.update {
                it.copy(isLoading = false, packages = response)
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
                deletePackageUseCase.invoke(packageId, fileId, imageId, jsonId)
            }
            if (result) {
                getData()
                onPackageDeleted()
            } else {
                //Handle Error
            }
        }
    }
}

data class HomeUIState(
    val packages: List<PackageModel> = emptyList(),
    val lastModifiedPackages: List<PackageModel> = emptyList(),
    val isLoading: Boolean = false
)