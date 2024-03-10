package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.ui.uimodel.AlignmentDataUIModel
import com.google.gson.Gson
import javax.inject.Inject

class GetAlignmentDataUseCase @Inject constructor(
    private val repository: DataBaseService
) {

    suspend operator fun invoke(idPackage: String): AlignmentDataUIModel {
        val fileContent = repository.getFileContent(idPackage)

        return if (fileContent.isNotBlank()) {
            val jsonName = "${idPackage}_json"
            val jsonContent = repository.getJsonContent(jsonName)
            val imageUri = repository.getImageUriFromPackage(idPackage)

            val gson = Gson()
            val alignmentJsonModel = gson.fromJson(jsonContent, AlignmentJsonModel::class.java)
            val elementIds = alignmentJsonModel.alignmentElements.map { it.elementId }

            AlignmentDataUIModel(fileContent, elementIds, imageUri)
        } else {
            AlignmentDataUIModel("", emptyList(), "")
        }
    }
}