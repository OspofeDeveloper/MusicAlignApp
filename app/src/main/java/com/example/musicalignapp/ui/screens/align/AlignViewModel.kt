package com.example.musicalignapp.ui.screens.align

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.domain.usecases.align.GetAlignmentDataUseCase
import com.example.musicalignapp.domain.usecases.align.SaveAlignmentResultsUseCase
import com.example.musicalignapp.ui.uimodel.AlignmentJsonUIModel
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
    private val saveAlignmentResultsUseCase: SaveAlignmentResultsUseCase,
    private val getAlignmentDataUseCase: GetAlignmentDataUseCase
) : ViewModel() {

    private var _uiState = MutableStateFlow(AlignUIState())
    val uiState: StateFlow<AlignUIState> = _uiState

    fun getData(packageId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getAlignmentDataUseCase(packageId)
            }
            if (result.fileContent.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        fileContent = result.fileContent,
                        listElementIds = result.listElements,
                        imageUrl = result.imageUri
                    )
                }
            } else {
                _uiState.update { it.copy( error = true ) }
                //Handle error with alertDialog
            }
        }
    }

    fun saveAlignmentResults(listElementIds: List<String>, packageId: String, onChangesSaved: () -> Unit) {
        val alignmentJsonUIModel = AlignmentJsonUIModel(packageId, listElementIds)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                saveAlignmentResultsUseCase(alignmentJsonUIModel.toDomain())
            }
            if (result) {
                onChangesSaved()
            } else {
                Log.d("AlignActivity", "Error in saving data")
            }
        }
    }
}

data class AlignUIState(
    val fileContent: String = "",
    val listElementIds: List<String> = emptyList(),
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: Boolean = false
)