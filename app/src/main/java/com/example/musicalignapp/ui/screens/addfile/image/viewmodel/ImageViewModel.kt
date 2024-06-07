package com.example.musicalignapp.ui.screens.addfile.image.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.domain.usecases.addfile.DeleteImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadOriginalImage
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
    private val uploadOriginalImage: UploadOriginalImage,
    private val deleteImageUseCase: DeleteImageUseCase,
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<ImageUIModel>>(ScreenState.Empty())
    val uiState: StateFlow<ScreenState<ImageUIModel>> = _uiState

    fun saveOriginalImage(imageUrl: Uri, imageName: String, onFinished: (ImageUIModel) -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()
            val result = withContext(Dispatchers.IO) {
                uploadOriginalImage(imageUrl, imageName)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                _uiState.value = ScreenState.Empty()
                onFinished(result)
            } else {
                _uiState.value = ScreenState.Error("Error")
            }
        }
    }

    fun deleteUploadedImage(imageId: String) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()

            val result = withContext(Dispatchers.IO) {
                deleteImageUseCase(imageId)
            }

            if (result) {
                _uiState.value = ScreenState.Empty()
            } else {
                _uiState.value = ScreenState.Error("Error")
            }
        }
    }
}