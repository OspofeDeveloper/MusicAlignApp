package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.domain.model.ProjectModel

interface HomeRepository {

    suspend fun getAllPackages(userId: String): ApiResult<List<ProjectHomeModel>>

    suspend fun deletePackage(packageId: String): Boolean

    suspend fun deleteFile(fileId: String): Boolean //TODO {Eliminar si no hace falta}

    suspend fun deleteImage(imageId: String): Boolean //TODO {Eliminar si no hace falta}

    suspend fun deleteJson(jsonId: String): Boolean //TODO {Eliminar si no hace falta}

    suspend fun getUserId(key: String): String
}