package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class UploadCropImage @Inject constructor(
    private val addFileRepository: AddFileRepository
) {
    suspend operator fun invoke(uri: Uri, cropImageName: String, imageName: String): Boolean {
        return addFileRepository.uploadCropImage(uri, cropImageName, imageName)
    }
}