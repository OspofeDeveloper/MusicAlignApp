package com.example.musicalignapp.ui.addfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.network.DataBaseService
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
            showLoading(true)
            val result = withContext(Dispatchers.IO) {
                repository.uploadAndDownloadImage(uri)
            }
            _uiState.update { it.copy(imageUrl = result) }
            showLoading(false)
        }
    }

    private fun showLoading(show: Boolean) {
        _uiState.update { it.copy(isLoading = show) }
    }

    fun onAddProductSelected(onSuccessProduct: () -> Unit) {
        viewModelScope.launch {
            showLoading(true)
            val result = withContext(Dispatchers.IO) {
                repository.uploadNewPackage(
                    _uiState.value.imageUrl,
                    _uiState.value.fileUrl,
                    _uiState.value.packageName,
                    _uiState.value.fileName
                )
            }

            if (result) {
                onSuccessProduct()
            } else {
                _uiState.update { it.copy(
                    error = "Ha ocurrido un error al intentar\n " +
                            "subir el paquete.\n " +
                            "Por favor, intentelo de nuevo")
                }
            }

            showLoading(false)
        }
    }
}

data class AddPackageState(
    val imageUrl: String = "",
    val packageName: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isValidPackage() = imageUrl.isNotBlank() && fileUrl.isNotBlank() && packageName.isNotBlank()
}