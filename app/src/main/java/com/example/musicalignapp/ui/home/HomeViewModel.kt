package com.example.musicalignapp.ui.home

import androidx.lifecycle.ViewModel
import com.example.musicalignapp.domain.model.PackageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

) : ViewModel() {

    private var _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState
}

data class HomeUIState(
    val packages: List<PackageModel> = emptyList(),
    val lastModifiedPackages: List<PackageModel> = emptyList()
)