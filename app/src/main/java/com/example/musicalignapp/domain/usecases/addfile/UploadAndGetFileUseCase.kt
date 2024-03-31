package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class UploadAndGetFileUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(uri: Uri, fileName: String) : FileModel {
        return repository.uploadAndGetFile(uri, fileName)
    }
}