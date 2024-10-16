package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.core.Constants.CURRENT_ELEMENT_SEPARATOR
import com.example.musicalignapp.core.Constants.USER_EMAIL_KEY
import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.di.InterfaceAppModule
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.domain.repository.FinalOutputRepository
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UploadPackageUseCase @Inject constructor(
    private val repository: AddFileRepository,
    private val finalOutputRepository: FinalOutputRepository,
    @InterfaceAppModule.AlignJsonConverterAnnotation private val jsonConverter: JsonConverter,
    @InterfaceAppModule.FinalOutputJsonConverterAnnotation private val finalJsonConverter: JsonConverter,
    private val sharedPreferences: SharedPreferences
) {
    suspend operator fun invoke(
        projectModel: ProjectModel,
        finalOutputJsonModel: FinalOutputJsonModel
    ): Boolean {
        val year = generateYear(projectModel.lastModified)
        val date = parseDate(projectModel.lastModified)

        val alignmentJsonList = generateAlignmentJson(projectModel)
        val finalJson =
            generateFinalOutputJson(finalOutputJsonModel, projectModel.projectName, year, date)

        val result = repository.uploadJsonFiles(alignmentJsonList)

        return if (result) {
            finalOutputRepository.uploadFinalOutputJson(finalJson)
            repository.uploadProject(
                projectModel.copy(jsonList = alignmentJsonList.toList()).toDto()
            )
        } else {
            false
        }
    }

    private fun generateYear(lastModified: String): Int {
        return lastModified.split(" ")[0].substringAfterLast("_").toInt()
    }

    private fun parseDate(dateString: String): Date {
        val inputFormat = SimpleDateFormat("dd_MM_yyyy HH:mm:ss", Locale.getDefault())
        return inputFormat.parse(dateString)
            ?: throw IllegalArgumentException("Invalid date format")
    }

    private fun generateAlignmentJson(projectModel: ProjectModel): List<JsonModel> {
        val alignmentJsonModel = AlignmentJsonModel(
            projectModel.projectName,
            "${projectModel.projectName}.00",
            "${CURRENT_ELEMENT_SEPARATOR}0",
            "${CURRENT_ELEMENT_SEPARATOR}0",
            emptyList()
        )
        val gson = Gson()
        val json = gson.toJson(alignmentJsonModel)
        val listOfJsons: MutableList<JsonModel> = mutableListOf()

        projectModel.filesList.forEachIndexed { index, _ ->
            val uri = jsonConverter.createJsonFile(json, projectModel.projectName)
            val jsonName = projectModel.projectName + ".${(index + 1).toTwoDigits()}.json"
            listOfJsons.add(JsonModel(projectModel.projectName, jsonName, uri))
        }

        return listOfJsons
    }

    private suspend fun generateFinalOutputJson(
        finalOutputJsonModel: FinalOutputJsonModel,
        projectName: String,
        year: Int,
        date: Date
    ): JsonModel {
        val finalJson = finalOutputJsonModel.copy(
            info = finalOutputJsonModel.info.copy(
                year = year,
                dateCreated = date,
                contributor = getUserEmail()
            ),
            images = finalOutputJsonModel.images.map { it.copy(dateCaptured = date) }
        )

        val gson = Gson()
        val json = gson.toJson(finalJson)
        val fileName = "${projectName}_final"
        val uri = finalJsonConverter.createFinalOutputJsonFile(json, fileName)
        val jsonName = "$fileName.json"

        return JsonModel(fileName, jsonName, uri)
    }

    private suspend fun getUserEmail(): String {
        return sharedPreferences.getUserEmail(USER_EMAIL_KEY)
    }
}