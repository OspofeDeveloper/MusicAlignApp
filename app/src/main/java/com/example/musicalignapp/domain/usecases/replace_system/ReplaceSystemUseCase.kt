package com.example.musicalignapp.domain.usecases.replace_system

import android.util.Log
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ReplaceSystemModel
import com.example.musicalignapp.domain.repository.ReplaceSystemRepository
import com.google.gson.Gson
import javax.inject.Inject

class ReplaceSystemUseCase @Inject constructor(
    private val replaceSystemRepository: ReplaceSystemRepository,
    private val jsonConverter: JsonConverter,
) {

    suspend operator fun invoke(replaceSystemModel: ReplaceSystemModel) {
        var isJsonUpdated = false

        if(replaceSystemModel.fileName.isNotBlank() && replaceSystemModel.fileUri.toString().isNotBlank()) {
            replaceSystemRepository.replaceFile(replaceSystemModel.fileName, replaceSystemModel.fileUri)

            val newJson = getJsonDto(replaceSystemModel.fileName)
            replaceSystemRepository.replaceJson(newJson)
            isJsonUpdated = true
        }
        if(replaceSystemModel.imageName.isNotBlank() && replaceSystemModel.imageUri.toString().isNotBlank()) {
            replaceSystemRepository.replaceImage(replaceSystemModel.imageName, replaceSystemModel.imageUri)
            if(!isJsonUpdated) {
                val newJson = getJsonDto(replaceSystemModel.imageName)
                replaceSystemRepository.replaceJson(newJson)
            }
        }
    }

    private fun getJsonDto(fileName: String) : JsonModel {
        val projectName = fileName.substringBeforeLast('.').substringBeforeLast('.')
        val systemNumber = fileName.substringBeforeLast('.').substringAfterLast('.')

        val alignmentJsonModel = AlignmentJsonModel(
            projectName,
            "${projectName}.00",
            "${Constants.CURRENT_ELEMENT_SEPARATOR}0",
            "${Constants.CURRENT_ELEMENT_SEPARATOR}0",
            emptyList()
        )
        val gson = Gson()
        val json = gson.toJson(alignmentJsonModel)

        val uri = jsonConverter.createJsonFile(json, projectName)
        val jsonName = projectName + ".${(systemNumber.toInt()).toTwoDigits()}.json"
        return JsonModel(projectName, jsonName, uri)
    }

}