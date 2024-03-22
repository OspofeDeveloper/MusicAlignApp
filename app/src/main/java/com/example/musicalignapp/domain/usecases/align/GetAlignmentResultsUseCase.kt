package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.data.shared.SharedPrefs
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.ui.uimodel.AlignmentDataUIModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetAlignmentDataUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val sharedPrefs: SharedPrefs
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

            val drawCoordinates: String = sharedPrefs.getDrawCoordinates().first() ?: ""

            AlignmentDataUIModel(fileContent, elementIds, imageUri, drawCoordinates)
        } else {
            AlignmentDataUIModel("", emptyList(), "", "")
        }
    }
}