package com.example.musicalignapp.ui.screens.addfile.image.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.remote.core.NetError
import com.example.musicalignapp.domain.usecases.addfile.DeleteImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadAndDownloadImageUseCase
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val deleteImageUseCase: DeleteImageUseCase,
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<ImageUIModel>>(ScreenState.Empty())
    val uiState: StateFlow<ScreenState<ImageUIModel>> = _uiState

    fun deleteUploadedImage(imageId: String, numImagesSaved: Int) {
        if(numImagesSaved == 1) {
            _uiState.value = ScreenState.Empty()
        } else {
            viewModelScope.launch {
                _uiState.value = ScreenState.Loading()

                val result = withContext(Dispatchers.IO) {
                    deleteImageUseCase(imageId)
                }

                if(result) {
                    _uiState.value = ScreenState.Empty()
                } else {
                    _uiState.value = ScreenState.Error("Error")
                }
            }
        }
    }
}