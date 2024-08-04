package com.example.musicalignapp.data.remote.repository

import com.example.musicalignapp.core.Constants.USER_EMAIL_KEY
import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.repository.FinalOutputRepository
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import javax.inject.Inject

class FinalOutputRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val databaseService: DataBaseService,
    private val sharedPreferences: SharedPreferences
): FinalOutputRepository {

    override suspend fun uploadFinalOutputJson(jsonModel: JsonModel) {
        storageService.uploadFinalOutputJson(jsonModel.toDto(), false, getUserId())
    }

    override suspend fun getFinalOutputJsonContent(packageName: String, finalOutputName: String): String {
        return databaseService.getFinalOutputJsonContent(packageName, finalOutputName, getUserId())
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(USER_ID_KEY)
    }
}