package com.example.musicalignapp.ui.screens.addfile.viewmodel

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants.REPEATED_PROJECT_SEPARATOR
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
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import com.example.musicalignapp.ui.uimodel.finaloutput.Image
import com.example.musicalignapp.ui.uimodel.finaloutput.Info
import com.example.musicalignapp.ui.uimodel.finaloutput.License
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

typealias Width = Int
typealias Height = Int

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

    private val _originalImageSize = MutableLiveData<Pair<Int, Int>>()
    val originalImageSize: LiveData<Pair<Int, Int>> get() = _originalImageSize

    private val _cropImageSize = MutableLiveData<Pair<Int, Int>>()
    val cropImageSize: LiveData<Pair<Int, Int>> get() = _cropImageSize

    private var originalImageUrl: Uri = "".toUri()

    private var _numImage: Int = 1

    private val imagesList: MutableList<ImageUIModel> = mutableListOf()
    private val outputImagesList: MutableList<Image> = mutableListOf()
    private var finalOutputImagesList: List<Image> = emptyList()

    fun onFileSelected(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _fileUIState.value = ScreenState.Loading()

            val fileSuffix = fileName.substringAfterLast(".")
            val fileSystemNumber = fileName.substringBeforeLast(".").substringAfterLast(".")
            val newFileName = if (imagesList.isEmpty()) {
                val listNames = withContext(Dispatchers.IO) {
                    getImagesNameListUseCase()
                }

                "${getImageName(listNames, fileName)}.$fileSuffix"
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

            if (result) {
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
                    imagesList.add(ImageUIModel(cropImage, originalImageUrl.toString()))
                    uploadCropImage(imageToCrop.value.second, cropImage)
                    finalOutputImagesList = outputImagesList.filter { it.id == 0 }
                } else {
                    finalOutputImagesList = outputImagesList.filter { it.id != 0 }
                }
                uploadPackageUseCase(
                    _packageState.value.copy(
                        imagesList = imagesList,
                        lastModified = packageDateGenerator.generate(),
                        isFinished = false,
                    ).toDomain(),
                    FinalOutputJsonModel(
                        info = Info(),
                        licenses = listOf(License()),
                        images = finalOutputImagesList
                    )
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
        originX: Int,
        originY: Int,
        cropImageName: String,
        onChangesSaved: (ImageUIModel) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val baseName = cropImageName.substringBeforeLast('.')
            val extension = cropImageName.substringAfterLast('.')

            val newCropImage = "${baseName}.${extension}"
            val result = withContext(Dispatchers.IO) {
                uploadCropImage(cropImageUri, newCropImage)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                _numImage += 1
                imagesList.add(result)
                _packageState.update { it.copy(imagesList = imagesList) }
                onChangesSaved(result.copy(originX = originX, originY = originY))
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

            val newImageName =
                "${getImageName(listNames, imageName)}.${imageName.substringAfterLast(".")}"
            val result = withContext(Dispatchers.IO) {
                uploadOriginalImage(imageUrl, newImageName)
            }
            if (result.id.isNotBlank() && result.imageUri.isNotBlank()) {
                originalImageUrl = result.imageUri.toUri()
                _uiState.value = ScreenState.Empty()
                onFinished(result)
            } else {
                _uiState.value = ScreenState.Error("Error")
            }
        }
    }

    fun getImageSize(imageUrl: String, isOriginal: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val width = bitmap.width
                val height = bitmap.height
                if(isOriginal) {
                    _originalImageSize.postValue(Pair(width, height))
                } else {
                    _cropImageSize.postValue(Pair(width, height))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onOriginalImageUploaded(image: ImageUIModel) {
        viewModelScope.launch {
            imagesList.add(image)
            outputImagesList.add(image.toFinalOutputImage(true))
            _packageState.update {
                it.copy(
                    imagesList = imagesList,
                    projectName = image.id.substringBeforeLast("."),
                    originalImageUrl = image.imageUri
                )
            }
        }
    }

    fun onCropImageUploaded(image: ImageUIModel) {
        outputImagesList.add(image.toFinalOutputImage(false))
    }

    fun onFileUploaded(filesList: List<FileUIModel>) {
        _packageState.update { it.copy(filesList = filesList) }
    }

    fun onFileDeleted(onFinish: () -> Unit) {
        _packageState.update { it.copy(filesList = emptyList()) }
        onFinish()
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

    fun getNumImage() = _numImage

    private fun getImageName(result: List<String>, imageId: String): String {
        var projectName = imageId.substringBeforeLast('.')
        val numbers: MutableList<String> = mutableListOf()
        val listProjectsWithSameName = result.filter { it.contains(projectName) }

        if (listProjectsWithSameName.isNotEmpty()) {
            listProjectsWithSameName.forEach {
                if (it.contains(REPEATED_PROJECT_SEPARATOR)) {
                    numbers.add(
                        (it.substringAfterLast(REPEATED_PROJECT_SEPARATOR).toInt() + 1).toString()
                    )
                } else {
                    numbers.add("1")
                }
            }
            val maxNumber = numbers.maxOrNull()?.toInt() ?: 0
            projectName = if (projectName.contains(REPEATED_PROJECT_SEPARATOR)) {
                "${projectName.substringBeforeLast(REPEATED_PROJECT_SEPARATOR)}$REPEATED_PROJECT_SEPARATOR$maxNumber"
            } else {
                "${projectName}$REPEATED_PROJECT_SEPARATOR$maxNumber"
            }
        }

        return projectName;
    }

    private fun onError(error: NetError) {
        _fileUIState.value = ScreenState.Error("error")
    }

    private fun onSuccess(data: FileUIModel) {
        _fileUIState.value = ScreenState.Success(data)
    }
}