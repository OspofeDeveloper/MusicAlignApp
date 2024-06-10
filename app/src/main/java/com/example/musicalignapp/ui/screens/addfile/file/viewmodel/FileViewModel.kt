package com.example.musicalignapp.ui.screens.addfile.file.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.remote.core.NetError
import com.example.musicalignapp.domain.usecases.addfile.DeleteUploadedFileUseCase
import com.example.musicalignapp.domain.usecases.addfile.UploadAndGetFileUseCase
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.uimodel.FileUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    private val deleteUploadedFileUseCase: DeleteUploadedFileUseCase,
) : ViewModel() {

    private var _uiState = MutableStateFlow<ScreenState<FileUIModel>>(ScreenState.Empty())
    val uiState: StateFlow<ScreenState<FileUIModel>> = _uiState


}