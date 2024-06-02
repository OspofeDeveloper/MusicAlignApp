package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.repository.AlignRepository
import com.google.gson.Gson
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    private val jsonConverter: JsonConverter,
    private val alignRepository: AlignRepository,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(alignmentResults: AlignmentJsonModel, projectModel: ProjectModel, saveChanges: Boolean): Boolean {
        val doSave: Boolean = !alignmentResults.highestElementId.endsWith("0")
        alignRepository.saveProject(projectModel)

        return if (doSave && saveChanges) {
            val gson = Gson()
            val json = gson.toJson(alignmentResults)
            val uri = jsonConverter.createJsonFile(json, alignmentResults.systemId)
            val jsonName = "${alignmentResults.systemId}.json"
            alignRepository.uploadJsonFile(JsonModel(alignmentResults.systemId, jsonName, uri))
        } else {
            true
        }
    }
}