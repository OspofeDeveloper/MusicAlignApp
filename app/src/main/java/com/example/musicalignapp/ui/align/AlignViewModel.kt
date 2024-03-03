package com.example.musicalignapp.ui.align

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.ui.home.HomeUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AlignViewModel @Inject constructor(
    private val repository: DataBaseService
): ViewModel() {

    private var _uiState = MutableStateFlow(AlignUIState())
    val uiState: StateFlow<AlignUIState> = _uiState

    fun getData(fileId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getFileContent(fileId)
            }
            if(!result.isNullOrBlank()) {
                _uiState.update { it.copy(fileContent = result) }
            } else {
                //Handle error with alertDialog
            }
        }
    }
}

data class AlignUIState(
    val fileContent: String = ""
)