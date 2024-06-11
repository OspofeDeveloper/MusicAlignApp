package com.example.musicalignapp.ui.screens.addfile.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.data.remote.core.NetError
import com.example.musicalignapp.di.InterfaceAppModule.PackageDateGeneratorAnnotation
import com.example.musicalignapp.domain.usecases.addfile.DeleteImageUseCase
import com.example.musicalignapp.domain.usecases.addfile.DeleteUploadedFileUseCase
import com.example.musicalignapp.domain.usecases.addfile.GetImagesNameListUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadAndGetFileUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadCropImage
import com.example.musicalignapp.domain.usecases.addfile.UploadOriginalImage
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
    private val uploadOriginalImage: UploadOriginalImage,
    private val uploadAndGetFileUseCase: UploadAndGetFileUseCase,
    private val deleteUploadedFileUseCase: DeleteUploadedFileUseCase,
    @PackageDateGeneratorAnnotation private val packageDateGenerator: Generator<String>
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<Boolean>>(ScreenState.Empty())
    val uiState: StateFlow<ScreenState<Boolean>> = _uiState

    private var _fileUIState = MutableStateFlow<ScreenState<FileUIModel>>(ScreenState.Empty())
    val fileUIState: StateFlow<ScreenState<FileUIModel>> = _fileUIState

    private var _packageState = MutableStateFlow(ProjectUIModel())
    val packageState: StateFlow<ProjectUIModel> = _packageState

    private var _imageToCrop = MutableStateFlow(Pair("", "".toUri()))
    val imageToCrop: StateFlow<Pair<String, Uri>> = _imageToCrop

    private var originalImageReference = ""

    private var _numImage: Int = 1

    private val imagesList: MutableList<ImageUIModel> = mutableListOf()

    fun onFileSelected(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _fileUIState.value = ScreenState.Loading()

            val fileSuffix = fileName.substringAfterLast(".")
            val fileSystemNumber = fileName.substringBeforeLast(".").substringAfterLast(".")
            val newFileName = if(imagesList.isEmpty()) {
                val listNames = withContext(Dispatchers.IO) {
                    getImagesNameListUseCase()
                }

                val newFileName = "${getImageName(listNames, fileName)}.$fileSuffix"
                newFileName
            } else {
                "${imagesList.first().id.substringBeforeLast(".")}.$fileSystemNumber.$fileSuffix"
            }
            withContext(Dispatchers.IO) {
                uploadAndGetFileUseCase(uri, newFileName).result(
                    ::onError, ::onSuccess
                )
            }
        }
    }

    fun deleteUploadedFile(fileId: String) {
        viewModelScope.launch {
            _fileUIState.value = ScreenState.Loading()

            val result = withContext(Dispatchers.IO) {
                deleteUploadedFileUseCase(fileId)
            }

            if(result) {
                _fileUIState.value = ScreenState.Empty()
            } else {
                _fileUIState.value = ScreenState.Error("Error")
            }
        }
    }

    fun onAddProductSelected() {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()

            val result = withContext(Dispatchers.IO) {
                if (imagesList.size == 1) {
                    val imageSuffix = _packageState.value.imagesList.first().id.substringAfterLast(".")
                    val cropImage = _packageState.value.imagesList.first().id.substringBeforeLast(".").plus(".01.$imageSuffix")
                    Log.d("Pozo", "Crop Image from package: $cropImage")
                    uploadCropImage(imageToCrop.value.second, cropImage)
                }
                Log.d("Pozo", "Package imagesList: ${packageState.value.imagesList}")
                uploadPackageUseCase(
                    _packageState.value.copy(
                        imagesList = imagesList,
                        lastModified = packageDateGenerator.generate(),
                        isFinished = false,
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
            Log.d("Pozo6", "CropImageName: $cropImageName")
            val baseName = cropImageName.substringBeforeLast('.')
            val extension = cropImageName.substringAfterLast('.')

            val newCropImage = "${baseName}.${extension}"
            Log.d("Pozo7", "newCropImage: $newCropImage, baseName: $baseName, extension: $extension")
            val result = withContext(Dispatchers.IO) {
                uploadCropImage(cropImageUri, newCropImage)
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

    fun saveOriginalImage(imageUrl: Uri, imageName: String, onFinished: (ImageUIModel) -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading()
            val listNames = withContext(Dispatchers.IO) {
                getImagesNameListUseCase()
            }

            val newImageName = "${getImageName(listNames, imageName)}.${imageName.substringAfterLast(".")}"
            Log.d("Pozo", "newImageName: $newImageName")
            Log.d("Pozo", "oldImageName: $imageName")
            val result = withContext(Dispatchers.IO) {
                uploadOriginalImage(imageUrl, newImageName)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                _uiState.value = ScreenState.Empty()
                onFinished(result)
            } else {
                _uiState.value = ScreenState.Error("Error")
            }
        }
    }

    fun onOriginalImageUploaded(image: ImageUIModel) {
        viewModelScope.launch {
            imagesList.add(image)
                _packageState.update {
                    it.copy(
                        imagesList = imagesList,
                        projectName = image.id.substringBeforeLast("."),
                        originalImageUrl = image.imageUri
                    )
                }
        }
    }

    fun onFileUploaded(filesList: List<FileUIModel>) {
        _packageState.update { it.copy(filesList = filesList) }
    }

    fun onFileDeleted(onFinish: () -> Unit) {
        _packageState.update { it.copy(filesList = emptyList()) }
        onFinish()
    }

    fun onNameChanged(projectName: CharSequence?) {
        _packageState.update { it.copy(projectName = projectName.toString()) }
    }

    fun setImageToCrop(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val listNames = withContext(Dispatchers.IO) {
                getImagesNameListUseCase()
            }
            imagesList.clear()
            val newImageName = getImageName(listNames, fileName)
            val extension = fileName.substringAfterLast('.')
            _imageToCrop.value = Pair("${newImageName}.$extension", uri)
        }


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

    private fun getImageName(result: List<String>, imageId: String): String {
        var projectName = imageId.substringBeforeLast('.')
        val numbers: MutableList<String> = mutableListOf()
        val listProjectsWithSameName = result.filter { it.contains(projectName) }

        if (listProjectsWithSameName.isNotEmpty()) {
            listProjectsWithSameName.forEach {
                if (it.contains("*")) {
                    numbers.add((it.substringAfterLast("*").toInt() + 1).toString())
                } else {
                    numbers.add("1")
                }
            }
            val maxNumber = numbers.maxOrNull()?.toInt() ?: 0
            projectName = if(projectName.contains("*")) {
                "${projectName.substringBeforeLast("*")}*$maxNumber"
            } else {
                "${projectName}*$maxNumber"
            }
        }

        return projectName;
    }

    private fun onError(error: NetError) {
        _fileUIState.value = ScreenState.Error("error")
    }

    private fun onSuccess(data: FileUIModel) {
        _fileUIState.value = ScreenState.Success(data)
        Log.d("Pozo3", "File data: $data")
    }
}