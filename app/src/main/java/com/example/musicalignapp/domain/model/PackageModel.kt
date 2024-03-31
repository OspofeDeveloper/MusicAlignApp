package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.remote.dto.PackageDto

data class PackageModel(
    val id: String,
    val imageUrl: String,
    val packageName: String,
    val fileName: String,
    val fileUrl: String,
    val lastModifiedDate: String,
    val fileId: String,
    val imageId: String,
    var jsonId: String
) {
    fun toDto(): PackageDto {
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