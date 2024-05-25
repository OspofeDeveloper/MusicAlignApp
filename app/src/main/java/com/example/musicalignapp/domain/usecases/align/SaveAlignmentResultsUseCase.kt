package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.google.gson.Gson
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    private val repository: StorageService,
    private val jsonConverter: JsonConverter,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(alignmentResults: AlignmentJsonModel): Boolean {
//        val doSave: Boolean = !alignmentResults.highestElementId.endsWith("0")
//
//        return if (doSave) {
//
//            val gson = Gson()
//            val json = gson.toJson(alignmentResults)
//            val uri = jsonConverter.createJsonFile(json, alignmentResults.packageId)
//            val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"
//
//            repository.uploadJsonFile(uri, jsonName, getUserId())
//        } else {
//            true
//        }
        return true
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(USER_ID_KEY)
    }

}