package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import javax.inject.Inject

class UploadOriginalImage @Inject constructor(
    private val addFileRepository: AddFileRepository
){
    suspend operator fun invoke(imageUrl: Uri, imageName: String): ImageUIModel {
        return addFileRepository.uploadOriginalImage(imageUrl, imageName).toUIModel()
    }
}