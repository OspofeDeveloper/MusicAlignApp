package com.example.musicalignapp.ui.screens.addfile.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.di.InterfaceAppModule.PackageDateGeneratorAnnotation
import com.example.musicalignapp.domain.usecases.addfile.DeleteImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.GetImagesNameListUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadCropImage
import com.example.musicalignapp.domain.usecases.addfile.UploadPackageUseCase
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.uimodel.FileUIModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddFileViewModel @Inject constructor(
    private val uploadPackageUseCase: UploadPackageUseCase,
    private val uploadCropImage: UploadCropImage,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val getImagesNameListUseCase: GetImagesNameListUseCase,
    @PackageDateGeneratorAnnotation private val packageDateGenerator: Generator<String>
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<Boolean>>(ScreenState.Empty())
    val uiState: StateFlow<ScreenState<Boolean>> = _uiState

    private var _packageState = MutableStateFlow(ProjectUIModel())
    val packageState: StateFlow<ProjectUIModel> = _packageState

    private var _imageToCrop = MutableStateFlow(Pair("", "".toUri()))
    val imageToCrop: StateFlow<Pair<String, Uri>> = _imageToCrop

    private var originalImageReference = ""

    private var _numImage: Int = 1

    private val imagesList: MutableList<ImageUIModel> = mutableListOf()

    fun onAddProductSelected() {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()

            val result = withContext(Dispatchers.IO) {
                if (imagesList.size == 1) {
                    val imageSuffix = _packageState.value.imagesList.first().id.substringAfterLast(".")
                    val cropImage = _packageState.value.imagesList.first().id.substringBeforeLast(".").plus(".01.$imageSuffix")
                    uploadCropImage(imageToCrop.value.second, cropImage)
                }
                uploadPackageUseCase(
                    _packageState.value.copy(
                        lastModified = packageDateGenerator.generate(),
                        isFinished = false
                    ).toDomain()
                )
            }
            if (result) {
                _uiState.value = ScreenState.Success(true)
            } else {
                _uiState.value = ScreenState.Error("Error")
            }
        }
    }

    fun saveCropImage(
        cropImageUri: Uri,
        cropImageName: String,
        onChangesSaved: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                uploadCropImage(cropImageUri, cropImageName)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                _numImage += 1
                imagesList.add(result)
                _packageState.update { it.copy(imagesList = imagesList) }
                onChangesSaved()
            } else {
                onError()
            }
        }
    }

    fun onOriginalImageUploaded(image: ImageUIModel) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getImagesNameListUseCase()
            }

            var projectName = image.id.substringBeforeLast('.')
//            image.id.substringBeforeLast('.').apply {
//                val containsName = result.filter { it.contains(this) }
//                if(containsName.isNotEmpty()) {
//                    projectName = if(containsName.count { it == "."} == 1) {
//                        "$this.v2"
//                    } else {
//                        "$this.${this.last().digitToInt() + 1}"
//                    }
//                }
//            }
            imagesList.add(image)
            _packageState.update {
                it.copy(
                    imagesList = imagesList,
                    projectName = projectName,
                    originalImageUrl = image.imageUri
                )
            }
        }
    }

    fun onFileUploaded(filesList: List<FileUIModel>) {
        _packageState.update { it.copy(filesList = filesList) }
    }

    fun onFileDeleted() {
        _packageState.update { it.copy(filesList = emptyList()) }
    }

    fun onNameChanged(projectName: CharSequence?) {
        _packageState.update { it.copy(projectName = projectName.toString()) }
    }

    fun setImageToCrop(uri: Uri, fileName: String) {
        _imageToCrop.value = Pair(fileName, uri)
    }

    fun deleteImage(onFinish: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()

            val result = withContext(Dispatchers.IO) {
                deleteImageUseCase("${packageState.value.projectName}.pnj")
            }

            if (result) {
                onFinish()
            } else {
                onError()
            }
        }
    }

    fun addOriginalImageFirebaseUri(uri: String) {
        Log.d("Pozo", "add $uri")
        originalImageReference = uri
    }

    fun getOriginalImageReference(): String {
        Log.d("Pozo", "get $originalImageReference")
        return originalImageReference
    }

    fun getNumImage() = _numImage

}