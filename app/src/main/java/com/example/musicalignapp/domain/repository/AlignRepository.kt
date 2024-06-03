package com.example.musicalignapp.domain.repository

import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import java.io.File

interface AlignRepository {
    suspend fun getSystemNumber(packageName: String): String
    suspend fun getMaxSystemNumber(packageName: String): String
    suspend fun saveProject(projectModel: ProjectModel): Boolean
    suspend fun getFile(packageName: String, systemName: String): String
    suspend fun getJsonContent(packageName: String, jsonName: String): String
    suspend fun getImageUriFromPackage(packageName: String, systemName: String): String
    suspend fun uploadJsonFile(json: JsonModel): Boolean
    suspend fun savePathsToShow(key: String, value: Int): Boolean
    suspend fun getPathsToShow(key: String): Int
    suspend fun getShowPaths(key: String): Boolean
    suspend fun saveShowPaths(showPaths: Boolean, key: String): Boolean
}