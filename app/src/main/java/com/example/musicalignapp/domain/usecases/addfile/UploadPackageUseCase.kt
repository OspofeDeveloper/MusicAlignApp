package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.example.musicalignapp.domain.model.AlignmentJsonModel
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.google.gson.Gson
import javax.inject.Inject

class UploadPackageUseCase @Inject constructor(
    private val repository: AddFileRepository,
    private val jsonConverter: JsonConverter,
) {

    suspend operator fun invoke(packageModel: PackageModel): Boolean {
        val alignmentJsonModel = AlignmentJsonModel(packageModel.id, "_0", "_0", emptyList(), emptyList())
        val gson = Gson()
        val json = gson.toJson(alignmentJsonModel)

        val uri = jsonConverter.createJsonFile(json, packageModel.id)
        val jsonName = uri.lastPathSegment!!.substringBefore("_json") + "_json"

        packageModel.jsonId = jsonName
        val result = repository.uploadJsonFile(uri, jsonName)

        return if (result) {
            repository.uploadNewPackage(packageModel.toDto())
        } else {
            false
        }
    }
}