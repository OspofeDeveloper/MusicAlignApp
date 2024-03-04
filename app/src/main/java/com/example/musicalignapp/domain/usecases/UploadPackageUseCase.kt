package com.example.musicalignapp.domain.usecases

import android.annotation.SuppressLint
import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.PackageModel
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class UploadPackageUseCase @Inject constructor(
    private val repository: DataBaseService,
    private val jsonConverter: JsonConverter
) {

    suspend operator fun invoke(packageModel: PackageModel): Boolean {
        val uri = jsonConverter.createJsonFile("", packageModel.id)
        val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"
        val result = repository.uploadJsonFile(uri, jsonName)

        return if(result) {
            repository.uploadNewPackage(packageModel.toDto(jsonName))
        } else {
            false
        }
    }
}