package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.domain.model.JsonModel

interface FinalOutputRepository {

    suspend fun uploadFinalOutputJson(jsonModel: JsonModel)

    suspend fun getFinalOutputJsonContent(packageName: String, finalOutputName: String): String
}