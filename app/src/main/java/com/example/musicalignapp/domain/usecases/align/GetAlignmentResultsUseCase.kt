package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.ui.uimodel.AlignmentDataUIModel
import com.google.gson.Gson
import javax.inject.Inject

class GetAlignmentDataUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(idPackage: String): AlignmentDataUIModel {
        val fileContent = repository.getFileContent(idPackage, getUserId())

        return if (fileContent.isNotBlank()) {
            val jsonName = "${idPackage}_json"
            val jsonContent = repository.getJsonContent(jsonName, getUserId())
            val imageUri = repository.getImageUriFromPackage(idPackage, getUserId())

            val gson = Gson()
            val alignmentJsonModel = gson.fromJson(jsonContent, AlignmentJsonModel::class.java)
            val elementIds = alignmentJsonModel.alignmentElements
            val elementStrokes = alignmentJsonModel.alignmentElementsStroke
            val lastElementId = alignmentJsonModel.lastElementId
            val highestElementId = alignmentJsonModel.highestElementId

            AlignmentDataUIModel(fileContent, elementIds, elementStrokes, lastElementId, highestElementId, imageUri)
        } else {
            AlignmentDataUIModel("", emptyList(), emptyList(), "","","")
        }
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(Constants.USER_ID_KEY)
    }
}