package com.example.musicalignapp.data.remote.repository

import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.repository.AlignRepository
import java.io.File
import javax.inject.Inject

class AlignRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val databaseService: DataBaseService,
    private val storageService: StorageService,
    private val sharedPreferences: SharedPreferences
) : AlignRepository {
    override suspend fun getSystemNumber(packageName: String): String {
        return firestoreService.getSystemNumber(packageName, getUserId())
    }

    override suspend fun getFile(packageName: String, systemName: String): String {
        return databaseService.getFile(packageName, systemName, getUserId())
    }

    override suspend fun getJsonContent(packageName: String, jsonName: String): String {
        return databaseService.getJsonContent(packageName, jsonName, getUserId())
    }

    override suspend fun getImageUriFromPackage(packageName: String, systemName: String): String {
        return databaseService.getImageUriFromPackage(packageName, systemName, getUserId())
    }

    override suspend fun uploadJsonFile(json: JsonModel): Boolean {
        return storageService.uploadJsonFile(json.toDto(), getUserId())
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(Constants.USER_ID_KEY)
    }

}