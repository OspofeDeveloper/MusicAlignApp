package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.google.gson.Gson
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val jsonConverter: JsonConverter,
) {

    suspend operator fun invoke(alignmentResults: AlignmentJsonModel): Boolean {
        return if (alignmentResults.alignmentElements.isNotEmpty()) {
            val gson = Gson()
            val json = gson.toJson(alignmentResults)
            val uri = jsonConverter.createJsonFile(json, alignmentResults.packageId)
            val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"

            repository.uploadJsonFile(uri, jsonName)
        } else {
            true
        }
    }

}