package com.example.musicalignapp.domain.usecases.align

import com.bugfender.sdk.Bugfender
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.repository.AlignRepository
import com.example.musicalignapp.domain.repository.FinalOutputRepository
import com.example.musicalignapp.ui.uimodel.AlignmentDataUIModel
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import com.example.musicalignapp.ui.uimodel.finaloutput.Info
import com.example.musicalignapp.utils.DateUtils
import com.google.gson.Gson
import javax.inject.Inject

class GetAlignmentResultsUseCase @Inject constructor(
    private val alignRepository: AlignRepository,
    private val finalOutputRepository: FinalOutputRepository,
) {

    suspend operator fun invoke(packageName: String): AlignmentDataUIModel {
        val currentSystem = alignRepository.getSystemNumber(packageName)
        val maxSystemNumber = alignRepository.getMaxSystemNumber(packageName)

        val systemName = "$packageName.$currentSystem"

        val file = alignRepository.getFile(packageName, systemName)
        return if (file.isNotBlank()) {
            val jsonName = "$systemName.json"
            val finalOutputName = "${packageName}_final.json"

            val jsonContent = alignRepository.getJsonContent(packageName, jsonName)
            val imageUri = alignRepository.getImageUriFromPackage(packageName, systemName)
            val finalOutputJsonModel = finalOutputRepository.getFinalOutputJsonContent(packageName, finalOutputName)

            val gson = Gson()
            val alignmentModel = gson.fromJson(jsonContent, AlignmentJsonModel::class.java)
            val finalOutputModel = gson.fromJson(finalOutputJsonModel, FinalOutputJsonModel::class.java)

            val elementIds = alignmentModel.alignmentElements
            val lastElementId = alignmentModel.lastElementId
            val highestElementId = alignmentModel.highestElementId

            val projectTimeInMillis = DateUtils.displayToMillis(finalOutputModel.projectDuration)

            Bugfender.d("Test", "currentSystem: $currentSystem, finalJsonImagesSize: ${finalOutputModel.images.size}")
            Bugfender.d("Test", "currentImageIndex: ${currentSystem.toInt() - 1}")

            finalOutputModel.images.forEachIndexed { index, image ->
                Bugfender.d("Test", "image $index: ${image.fileName}")
            }

            val currentImageId = finalOutputModel.images[currentSystem.toInt() - 1].id

            AlignmentDataUIModel(
                file,
                currentSystem,
                elementIds,
                maxSystemNumber,
                lastElementId,
                highestElementId,
                imageUri,
                currentImageId,
                finalOutputModel,
                projectTimeInMillis
            )
        } else {
            AlignmentDataUIModel(null, "", emptyList(), "","", "", "", 0, FinalOutputJsonModel(Info(), licenses = emptyList()), 0L)
        }
    }
}