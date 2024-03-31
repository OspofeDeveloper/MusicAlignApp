package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class UploadAndDownloadImageUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(uri: Uri): ImageModel {
        return repository.uploadAndDownloadImage(uri)
    }
}