package com.example.musicalignapp.data.remote.repository

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.tryCall
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val databaseService: DataBaseService
) : HomeRepository {

    override suspend fun getAllPackages(): ApiResult<List<PackageModel>> {
        return tryCall {
            firestoreService.getAllPackages().map { it.toDomain() }
        }
    }

    override suspend fun deletePackage(packageId: String): Boolean =
        databaseService.deletePackage(packageId)

    override suspend fun deleteFile(fileId: String): Boolean =
        databaseService.deleteFile(fileId)

    override suspend fun deleteImage(imageId: String): Boolean =
        databaseService.deleteImage(imageId)

    override suspend fun deleteJson(jsonId: String): Boolean =
        databaseService.deleteJson(jsonId)

}