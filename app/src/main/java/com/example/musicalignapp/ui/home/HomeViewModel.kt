package com.example.musicalignapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.PackageModel
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
    private val repository: DataBaseService
) : ViewModel() {

    private var _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState

    init {
        getData()
    }

    private fun getData() {
        gatAllPackages()
    }

    private fun gatAllPackages() {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                repository.getAllPackages()
            }
            _uiState.update { it.copy(packages = response) }
        }
    }
}

data class HomeUIState(
    val packages: List<PackageModel> = emptyList(),
    val lastModifiedPackages: List<PackageModel> = emptyList()
)