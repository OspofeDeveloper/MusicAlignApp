package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.domain.model.PackageModel

interface HomeRepository {

    suspend fun getAllPackages(): ApiResult<List<PackageModel>>

    suspend fun deletePackage(packageId: String): Boolean

    suspend fun deleteFile(fileId: String): Boolean

    suspend fun deleteImage(imageId: String): Boolean

    suspend fun deleteJson(jsonId: String): Boolean
}