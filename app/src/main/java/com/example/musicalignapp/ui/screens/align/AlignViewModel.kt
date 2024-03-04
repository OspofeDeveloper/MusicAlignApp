package com.example.musicalignapp.ui.screens.align

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentElements
import com.example.musicalignapp.domain.model.AlignmentModel
import com.example.musicalignapp.domain.usecases.SaveAlignmentResultsUseCase
import com.example.musicalignapp.ui.screens.home.HomeUIState
import com.example.musicalignapp.ui.uimodel.AlignmentUIModel
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
    private val repository: DataBaseService,
    private val saveAlignmentResultsUseCase: SaveAlignmentResultsUseCase
): ViewModel() {

    private var _uiState = MutableStateFlow(AlignUIState())
    val uiState: StateFlow<AlignUIState> = _uiState

    fun getData(id: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getFileContent(id)
            }
            if(!result.isNullOrBlank()) {
                _uiState.update { it.copy(fileContent = result) }
            } else {
                //Handle error with alertDialog
            }
        }
    }

    fun saveAlignmentResults(listElementIds: List<String>, packageId: String) {
        val alignmentUIModel = AlignmentUIModel(packageId, listElementIds)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                saveAlignmentResultsUseCase(alignmentUIModel.toDomain())
            }
            if(result) {
                //Mostrar guardado correcto
            } else {
                //Mostrar guardado no correcto
            }
        }
    }
}

data class AlignUIState(
    val fileContent: String = "",
)