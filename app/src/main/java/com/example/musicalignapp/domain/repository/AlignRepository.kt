package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.domain.model.JsonModel
import java.io.File

interface AlignRepository {
    suspend fun getSystemNumber(packageName: String): String
    suspend fun getFile(packageName: String, systemName: String): String
    suspend fun getJsonContent(packageName: String, jsonName: String): String
    suspend fun getImageUriFromPackage(packageName: String, systemName: String): String
    suspend fun uploadJsonFile(json: JsonModel): Boolean
}