package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.domain.model.PackageModel
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    suspend fun getAllPackages(userId: String): ApiResult<List<PackageModel>>

    suspend fun deletePackage(packageId: String): Boolean

    suspend fun deleteFile(fileId: String): Boolean

    suspend fun deleteImage(imageId: String): Boolean

    suspend fun deleteJson(jsonId: String): Boolean

    suspend fun getUserId(key: String): String
}