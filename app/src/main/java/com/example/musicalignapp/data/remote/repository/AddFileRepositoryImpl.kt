package com.example.musicalignapp.data.remote.repository

import android.net.Uri
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class AddFileRepositoryImpl @Inject constructor(
    private val dataBaseService: DataBaseService
) : AddFileRepository{

    override suspend fun deleteFile(fileId: String): Boolean {
        return dataBaseService.deleteFile(fileId)
    }

    override suspend fun deleteImage(imageId: String): Boolean {
        return dataBaseService.deleteImage(imageId)
    }

    override suspend fun uploadAndDownloadImage(uri: Uri): ImageModel {
        return dataBaseService.uploadAndDownloadImage(uri)
    }

    override suspend fun uploadAndGetFile(uri: Uri, fileName: String): FileModel {
        return dataBaseService.uploadAngGetFile(uri, fileName)
    }
}