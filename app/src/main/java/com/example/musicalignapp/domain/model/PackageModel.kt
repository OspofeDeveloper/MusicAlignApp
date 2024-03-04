package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.response.PackageDto

data class PackageModel (
    val id: String,
    val imageUrl: String,
    val packageName: String,
    val fileName: String,
    val fileUrl: String,
    val lastModifiedDate: String,
    val fileId: String,
    val imageId: String
) {
    fun toDto(jsonId: String): PackageDto {
        return PackageDto(
            id = id,
            imageUrl = imageUrl,
            packageName = packageName,
            fileName = fileName,
            fileUrl = fileUrl,
            lastModifiedDate = lastModifiedDate,
            imageId = imageId,
            fileId = fileId,
            jsonId = jsonId
        )
    }
}