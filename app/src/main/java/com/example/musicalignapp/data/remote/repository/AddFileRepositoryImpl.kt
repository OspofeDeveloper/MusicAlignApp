package com.example.musicalignapp.data.remote.repository

import android.net.Uri
import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.tryCall
import com.example.musicalignapp.data.remote.dto.ProjectDto
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class AddFileRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val firestoreService: FirestoreService,
    private val sharedPreferences: SharedPreferences
) : AddFileRepository {

    override suspend fun deleteFile(fileId: String): Boolean {
        return storageService.deleteFile(fileId, getUserId())
    }

    override suspend fun deleteImage(imageId: String): Boolean {
        return storageService.deleteImage(imageId, getUserId())
    }

    override suspend fun uploadAndDownloadImage(uri: Uri): ApiResult<ImageModel> {
        return tryCall {
            storageService.uploadAndDownloadImage(uri, getUserId())
        }
    }

    override suspend fun uploadAndGetFile(uri: Uri, fileName: String): ApiResult<FileModel> {
        return tryCall {
            storageService.uploadAngGetFile(uri, fileName, getUserId())
        }
    }

    override suspend fun uploadJsonFiles(jsonsList: List<JsonModel>): Boolean {
        return storageService.uploadJsonFiles(jsonsList.map {it.toDto()}, getUserId())
    }

    override suspend fun uploadProject(projectDto: ProjectDto): Boolean {
        return firestoreService.uploadProject(projectDto, getUserId())
    }

    override suspend fun uploadCropImage(uri: Uri, cropImageName: String): ImageModel {
        return storageService.uploadCropImage(uri, cropImageName, getUserId()).toDomain()
    }

    override suspend fun uploadOriginalImage(imageUrl: Uri, imageName: String): ImageModel {
        return storageService.uploadOriginalImage(imageUrl, imageName, getUserId()).toDomain()
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(USER_ID_KEY)
    }
}