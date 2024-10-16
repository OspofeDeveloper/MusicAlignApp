package com.example.musicalignapp.domain.usecases.finaloutput

import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.di.InterfaceAppModule
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.repository.FinalOutputRepository
import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import com.google.gson.Gson
import javax.inject.Inject

class CreateFinalOutputJsonUseCase @Inject constructor(
    private val finalOutputRepository: FinalOutputRepository,
    @InterfaceAppModule.FinalOutputJsonConverterAnnotation private val jsonConverter: JsonConverter,
) {

    suspend operator fun invoke(finalOutputJsonModel: FinalOutputJsonModel, fileName: String) {
        val gson = Gson()
        val json = gson.toJson(finalOutputJsonModel)
        val uri = jsonConverter.createFinalOutputJsonFile(json, fileName)
        val jsonName = "$fileName.json"

        val finalJson = JsonModel(fileName, jsonName, uri)

        finalOutputRepository.uploadFinalOutputJson(finalJson)
    }
}