package com.example.musicalignapp.domain.usecases

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentModel
import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.util.Objects
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val jsonConverter: JsonConverter
) {

    suspend operator fun invoke(alignmentResults: AlignmentModel): Boolean {
        val gson = Gson()
        val json = gson.toJson(alignmentResults)
        val uri = jsonConverter.createJsonFile(json, alignmentResults.packageId)
        val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"

        return repository.uploadJsonFile(uri, jsonName)
    }

}