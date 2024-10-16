package com.example.musicalignapp.domain.repository

import android.net.Uri
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.JsonModel

interface ReplaceSystemRepository {
    suspend fun replaceFile(fileName: String, fileUrl: Uri): Boolean
    suspend fun replaceImage(imageName: String, imageUri: Uri): ImageModel
    suspend fun replaceJson(newJson: JsonModel)
}