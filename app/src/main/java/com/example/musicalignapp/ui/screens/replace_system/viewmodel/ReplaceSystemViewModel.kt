package com.example.musicalignapp.ui.screens.replace_system.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants.REPEATED_PROJECT_SEPARATOR
import com.example.musicalignapp.domain.usecases.replace_system.ReplaceSystemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReplaceSystemViewModel @Inject constructor(
    private val replaceSystemUseCase: ReplaceSystemUseCase,
) : ViewModel() {

    private var _replaceSysState = MutableStateFlow(ReplaceSystemState())
    val replaceSysState: StateFlow<ReplaceSystemState> = _replaceSysState

    private var _imageToCrop = MutableStateFlow(Pair("", "".toUri()))
    val imageToCrop: StateFlow<Pair<String, Uri>> = _imageToCrop

    var _systemName: String = ""

    fun onImageSelected(uri: Uri, imageSuffix: String?) {
        val imageName = imageSuffix?.let {
            "$_systemName.$imageSuffix"
        } ?: run {
            _replaceSysState.value.imageName
        }

        _imageToCrop.value = Pair(imageName, uri)

        _replaceSysState.update {
            it.copy(
                imageUri = uri,
                imageName = imageName,
            )
        }
    }


    fun onFileSelected(uri: Uri, fileSuffix: String, fileName: String, onFailure: (String) -> Unit) {
        val expectedFileName = if(_systemName.contains(REPEATED_PROJECT_SEPARATOR)) {
            "${_systemName.substringBeforeLast(REPEATED_PROJECT_SEPARATOR)}.${_systemName.substringAfterLast(".")}"
        } else {
            _systemName
        }

        if(fileName.substringBeforeLast(".") == expectedFileName) {
            _replaceSysState.update {
                it.copy(
                    fileUri = uri,
                    fileName = "$_systemName.$fileSuffix",
                )
            }
        } else {
            onFailure(expectedFileName)
        }
    }

    fun deleteImage() {
        _replaceSysState.update {
            it.copy(
                imageUri = "".toUri(),
                imageName = "",
            )
        }
        _imageToCrop.value = Pair("", "".toUri())
    }

    fun deleteFile() {
        _replaceSysState.update {
            it.copy(
                fileUri = "".toUri(),
                fileName = "",
            )
        }
    }

    fun onReplaceSystemSelected(onResult: () -> Unit) {
        viewModelScope.launch {
            _replaceSysState.update { it.copy(loading = true) }
            withContext(Dispatchers.IO) {
                replaceSystemUseCase(replaceSysState.value.toDomain())
            }
            _replaceSysState.update { it.copy(loading = false) }
            onResult()
        }
    }
    fun setSystemName(systemName: String) {
        _systemName = systemName
    }
}