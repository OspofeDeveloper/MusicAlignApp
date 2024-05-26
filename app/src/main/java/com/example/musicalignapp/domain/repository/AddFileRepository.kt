package com.example.musicalignapp.domain.repository

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.dto.ProjectDto
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.JsonModel

interface AddFileRepository {

    suspend fun deleteFile(fileId: String): Boolean

    suspend fun deleteImage(imageId: String): Boolean

    suspend fun uploadAndDownloadImage(uri: Uri): ApiResult<ImageModel>

    suspend fun uploadAndGetFile(uri: Uri, fileName: String): ApiResult<FileModel>

    suspend fun uploadJsonFiles(jsonsList: List<JsonModel>): Boolean

    suspend fun uploadProject(projectDto: ProjectDto): Boolean

    suspend fun uploadCropImage(uri: Uri, cropImageName: String): ImageModel

    suspend fun uploadOriginalImage(imageUrl: Uri, imageName: String): ImageModel
}