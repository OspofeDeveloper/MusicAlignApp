package com.example.musicalignapp.ui.screens.addfile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.id_generators.IdGenerator
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.domain.usecases.addfile.DeleteImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.DeleteUploadedFileUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadAndDownloadImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadAndGetFileUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadPackageUseCase
import com.example.musicalignapp.ui.uimodel.AddFileUIModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddFileViewModel @Inject constructor(
    private val uploadPackageUseCase: UploadPackageUseCase,
    private val deleteUploadedFileUseCase: DeleteUploadedFileUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val uploadAndDownloadImageUseCase: UploadAndDownloadImageUseCase,
    private val uploadAndGetFileUseCase: UploadAndGetFileUseCase,
    private val idGenerator: IdGenerator
) : ViewModel() {

    private var _uiState = MutableStateFlow(AddFileUIModel())
    val uiState: StateFlow<AddFileUIModel> = _uiState

    fun onNameChanged(packageName: CharSequence?) {
        _uiState.update { it.copy(packageName = packageName.toString()) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true) }
            val result = withContext(Dispatchers.IO) {
                uploadAndDownloadImageUseCase(uri)
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
                uploadAndGetFileUseCase(uri, fileName)
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
                uploadPackageUseCase(PackageModel(
                    imageUrl = _uiState.value.storageImage.imageUri,
                    fileUrl = _uiState.value.storageFile.fileUri,
                    packageName = _uiState.value.packageName,
                    fileName = _uiState.value.storageFile.fileName,
                    imageId = _uiState.value.storageImage.id,
                    fileId = _uiState.value.storageFile.id,
                    id = idGenerator.generatePackageId(),
                    lastModifiedDate = generatePackageDate(),
                    jsonId = ""
                ))
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
                deleteUploadedFileUseCase(fileId)
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
                deleteImageUseCase(imageId)
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

    @SuppressLint("SimpleDateFormat")
    private fun generatePackageDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
    }
}