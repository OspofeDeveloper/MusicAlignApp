package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.data.shared.SharedPrefs
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.google.gson.Gson
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val jsonConverter: JsonConverter,
    private val sharedPrefs: SharedPrefs
) {

    suspend operator fun invoke(alignmentResults: AlignmentJsonModel, currentPath: String): Boolean {
        val gson = Gson()
        val json = gson.toJson(alignmentResults)
        val uri = jsonConverter.createJsonFile(json, alignmentResults.packageId)
        val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"
        sharedPrefs.saveDrawCoordinates(currentPath)

        return repository.uploadJsonFile(uri, jsonName)
    }

}