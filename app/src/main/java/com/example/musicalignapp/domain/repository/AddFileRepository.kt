package com.example.musicalignapp.domain.repository

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel

interface AddFileRepository {

    suspend fun deleteFile(fileId: String): Boolean

    suspend fun deleteImage(imageId: String): Boolean

    suspend fun uploadAndDownloadImage(uri: Uri): ApiResult<ImageModel>

    suspend fun uploadAndGetFile(uri: Uri, fileName: String): ApiResult<FileModel>
}