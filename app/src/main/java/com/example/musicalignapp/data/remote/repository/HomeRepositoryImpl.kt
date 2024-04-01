package com.example.musicalignapp.data.remote.repository

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.tryCall
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService
) : HomeRepository {

    override suspend fun getAllPackages(): ApiResult<List<PackageModel>> {
        return tryCall {
            firestoreService.getAllPackages().map { it.toDomain() }
        }
    }

    override suspend fun deletePackage(packageId: String): Boolean =
        firestoreService.deletePackage(packageId)

    override suspend fun deleteFile(fileId: String): Boolean =
        storageService.deleteFile(fileId)

    override suspend fun deleteImage(imageId: String): Boolean =
        storageService.deleteImage(imageId)

    override suspend fun deleteJson(jsonId: String): Boolean =
        storageService.deleteJson(jsonId)

}