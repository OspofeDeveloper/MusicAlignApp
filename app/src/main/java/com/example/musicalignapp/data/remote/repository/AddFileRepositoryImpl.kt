package com.example.musicalignapp.data.remote.repository

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.tryCall
import com.example.musicalignapp.data.remote.dto.PackageDto
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class AddFileRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val firestoreService: FirestoreService
) : AddFileRepository{

    override suspend fun deleteFile(fileId: String): Boolean {
        return storageService.deleteFile(fileId)
    }

    override suspend fun deleteImage(imageId: String): Boolean {
        return storageService.deleteImage(imageId)
    }

    override suspend fun uploadAndDownloadImage(uri: Uri): ApiResult<ImageModel> {
        return tryCall {
            storageService.uploadAndDownloadImage(uri)
        }
    }

    override suspend fun uploadAndGetFile(uri: Uri, fileName: String): ApiResult<FileModel> {
        return tryCall {
            storageService.uploadAngGetFile(uri, fileName)
        }
    }

    override suspend fun uploadJsonFile(uri: Uri, jsonName: String): Boolean {
        return storageService.uploadJsonFile(uri, jsonName)
    }

    override suspend fun uploadNewPackage(projectDto: PackageDto): Boolean {
        return firestoreService.uploadNewPackage(projectDto)
    }
}