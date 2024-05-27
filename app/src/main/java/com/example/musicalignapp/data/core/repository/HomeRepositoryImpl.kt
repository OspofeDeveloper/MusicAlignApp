package com.example.musicalignapp.data.core.repository

import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.tryCall
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService,
    private val sharedPreferences: SharedPreferences
) : HomeRepository {

    override suspend fun getAllPackages(userId: String): ApiResult<List<ProjectHomeModel>> {
        return tryCall {
            firestoreService.getAllPackages(userId).map { it.toProjectHomeModel() }
        }
    }

    override suspend fun deletePackage(packageId: String): Boolean {
        return firestoreService.deletePackage(packageId, getUserId(USER_ID_KEY))
    }

    override suspend fun deleteFile(fileId: String): Boolean {
        return storageService.deleteFile(fileId, getUserId(USER_ID_KEY))
    }

    override suspend fun deleteImage(imageId: String): Boolean {
        return storageService.deleteImage(imageId, getUserId(USER_ID_KEY))
    }

    override suspend fun deleteJson(jsonId: String): Boolean {
        return storageService.deleteJson(jsonId, getUserId(USER_ID_KEY))
    }

    override suspend fun getUserId(key: String): String {
        return sharedPreferences.getUserId(key)
    }
}