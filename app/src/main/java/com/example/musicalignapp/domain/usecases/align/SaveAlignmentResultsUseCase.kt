package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants.USER_ID_KEY
import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.di.InterfaceAppModule
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.domain.repository.AlignRepository
import com.example.musicalignapp.domain.repository.FinalOutputRepository
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import com.google.gson.Gson
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    @InterfaceAppModule.AlignJsonConverterAnnotation private val jsonConverter: JsonConverter,
    @InterfaceAppModule.FinalOutputJsonConverterAnnotation private val finalJsonConverter: JsonConverter,
    private val alignRepository: AlignRepository,
    private val finalOutputRepository: FinalOutputRepository,
) {

    suspend operator fun invoke(
        alignmentResults: AlignmentJsonModel,
        projectModel: ProjectModel,
        saveChanges: Boolean,
        finalOutputJsonModel: FinalOutputJsonModel
    ): Boolean {
        val doSave: Boolean = !alignmentResults.highestElementId.endsWith("0")
        alignRepository.saveProject(projectModel)

        return if (doSave && saveChanges) {
            val gson = Gson()
            val alignmentJson = generateAlignmentJson(alignmentResults, gson)
            val finalOutputJson = generateFinalOutputJson(finalOutputJsonModel, projectModel.projectName, gson)
            finalOutputRepository.uploadFinalOutputJson(finalOutputJson)
            alignRepository.uploadJsonFile(alignmentJson)
        } else {
            true
        }
    }

    private fun generateAlignmentJson(alignmentResults: AlignmentJsonModel, gson: Gson): JsonModel {
        val json = gson.toJson(alignmentResults)
        val uri = jsonConverter.createJsonFile(json, alignmentResults.systemId)
        val jsonName = "${alignmentResults.systemId}.json"

        return JsonModel(alignmentResults.systemId, jsonName, uri)
    }

    private fun generateFinalOutputJson(jsonModel: FinalOutputJsonModel, projectName: String, gson: Gson): JsonModel {
        val json = gson.toJson(jsonModel)
        val fileName = "${projectName}_final"
        val uri = finalJsonConverter.createFinalOutputJsonFile(json, fileName)
        val jsonName = "$fileName.json"

        return JsonModel(fileName, jsonName, uri)
    }
}