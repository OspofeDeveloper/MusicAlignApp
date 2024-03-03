package com.example.musicalignapp.ui.screens.addfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddFileViewModel @Inject constructor(
    private val repository: DataBaseService
) : ViewModel() {

    private var _uiState = MutableStateFlow(AddPackageState())
    val uiState: StateFlow<AddPackageState> = _uiState

    fun onNameChanged(packageName: CharSequence?) {
        _uiState.update { it.copy(packageName = packageName.toString()) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true) }
            val result = withContext(Dispatchers.IO) {
                repository.uploadAndDownloadImage(uri)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                _uiState.update { it.copy(storageImage = ImageModel(result.id, result.imageUri), isImageUploading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        error = "Ha ocurrido un error al intentar\n " +
                                "cargar la imagen.\n " +
                                "Por favor, intentelo de nuevo",
                        isImageUploading = false
                    )
                }
            }
        }
    }

    fun onFileSelected(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFileUploading = true) }

            val response = withContext(Dispatchers.IO) {
                repository.uploadAngGetFile(uri, fileName)
            }
            if (response.id.isNotBlank() && response.fileUri.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        isFileUploading = false,
                        storageFile = FileUIModel(
                            id = response.id,
                            fileName = fileName,
                            fileUri = response.fileUri
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = "Ha ocurrido un error al intentar\n " +
                                "cargar el fichero.\n " +
                                "Por favor, intentelo de nuevo",
                        isFileUploading = false
                    )
                }
            }
        }
    }

    fun onAddProductSelected(onSuccessProduct: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPackageLoading = true) }
            val result = withContext(Dispatchers.IO) {
                repository.uploadNewPackage(
                    _uiState.value.storageImage.imageUri,
                    _uiState.value.storageFile.fileUri,
                    _uiState.value.packageName,
                    _uiState.value.storageFile.fileName
                )
            }

            if (result) {
                onSuccessProduct()
            } else {
                _uiState.update {
                    it.copy(
                        error = "Ha ocurrido un error al intentar\n " +
                                "subir el paquete.\n " +
                                "Por favor, intentelo de nuevo"
                    )
                }
            }

            _uiState.update { it.copy(isPackageLoading = false) }
        }
    }

    fun deleteUploadedFile(fileId: String, onFileDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFileDeleting = true) }
            val result = withContext(Dispatchers.IO) {
                repository.deleteFile(fileId)
            }
            if (result) {
                onFileDeleted()
                _uiState.update {
                    it.copy(
                        storageFile = FileUIModel("", "", ""),
                        isFileDeleting = false
                    )
                }
            } else {
                //Handle error
            }
        }
    }

    fun deleteUploadedImage(imageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageDeleting = true) }
            val result = withContext(Dispatchers.IO) {
                repository.deleteImage(imageId)
            }
            if (result) {
                _uiState.update {
                    it.copy(
                        storageImage = ImageModel("", ""),
                        isImageDeleting = false
                    )
                }
            } else {
                //Handle error
            }
        }
    }
}

data class AddPackageState(
    val packageName: String = "",
    val storageFile: FileUIModel = FileUIModel("", "", ""),
    val storageImage: ImageModel = ImageModel("", ""),
    val isImageUploading: Boolean = false,
    val isImageDeleting: Boolean = false,
    val isFileUploading: Boolean = false,
    val isFileDeleting: Boolean = false,
    val isPackageLoading: Boolean = false,
    val error: String? = null
) {
    fun isValidPackage() =
        storageImage.imageUri.isNotBlank() && storageFile.fileUri.isNotBlank() && packageName.isNotBlank() && storageFile.fileName.isNotBlank()
}