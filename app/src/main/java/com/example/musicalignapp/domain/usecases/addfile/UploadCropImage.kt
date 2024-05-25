package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.error
import com.example.musicalignapp.data.remote.core.success
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import javax.inject.Inject

class UploadCropImage @Inject constructor(
    private val addFileRepository: AddFileRepository
) {
    suspend operator fun invoke(uri: Uri, cropImageName: String): ImageUIModel {
        return addFileRepository.uploadCropImage(uri, cropImageName).toUIModel()
    }
}