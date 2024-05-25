package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.google.gson.Gson
import javax.inject.Inject

class UploadPackageUseCase @Inject constructor(
    private val repository: AddFileRepository,
    private val jsonConverter: JsonConverter,
) {
    suspend operator fun invoke(projectModel: ProjectModel): Boolean {
        val alignmentJsonModel = AlignmentJsonModel(projectModel.projectName, "_0", "_0", emptyList(), emptyList())
        val gson = Gson()
        val json = gson.toJson(alignmentJsonModel)
        val listOfJsons: MutableList<JsonModel> = mutableListOf()

        projectModel.filesList.forEachIndexed { index, _ ->
            val uri = jsonConverter.createJsonFile(json, projectModel.projectName)
            val jsonName = projectModel.projectName + ".${(index + 1).toTwoDigits()}.json"
            listOfJsons.add(JsonModel(projectModel.projectName, jsonName, uri))
        }

        val result = repository.uploadJsonFiles(listOfJsons)

        return if (result) {
            repository.uploadProject(projectModel.copy(jsonList = listOfJsons.toList()).toDto())
        } else {
            false
        }
    }
}