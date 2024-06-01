package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.extensions.getContent
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.repository.AlignRepository
import com.example.musicalignapp.ui.uimodel.AlignmentDataUIModel
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class GetAlignmentDataUseCase @Inject constructor(
    private val alignRepository: AlignRepository,
) {

    suspend operator fun invoke(packageName: String): AlignmentDataUIModel {
        val currentSystem = alignRepository.getSystemNumber(packageName)

        val systemName = "$packageName.$currentSystem"

        val file = alignRepository.getFile(packageName, systemName)
        return if (file.isNotBlank()) {
            val jsonName = "$systemName.json"
            val jsonContent = alignRepository.getJsonContent(packageName, jsonName)
            val imageUri = alignRepository.getImageUriFromPackage(packageName, systemName)

            val gson = Gson()
            val alignmentModel = gson.fromJson(jsonContent, AlignmentJsonModel::class.java)

            val elementIds = alignmentModel.alignmentElements
//            val elementStrokes = alignmentModel.alignmentElementsStroke
            val lastElementId = alignmentModel.lastElementId
            val highestElementId = alignmentModel.highestElementId

            AlignmentDataUIModel(
                file,
                currentSystem,
                elementIds,
//                elementStrokes,
                lastElementId,
                highestElementId,
                imageUri
            )
        } else {
//            AlignmentDataUIModel(null, "", emptyList(), emptyList(), "", "", "")
            AlignmentDataUIModel(null, "", emptyList(), "", "", "")
        }
    }
}