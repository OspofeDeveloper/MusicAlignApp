package com.example.musicalignapp.data.response

import com.example.musicalignapp.domain.model.PackageModel

data class PackageDto(
    val id: String = "",
    val imageUrl: String = "",
    val packageName: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val lastModifiedDate: String = "",
    val imageId: String = "",
    val fileId: String = "",
    val jsonId: String = ""
) {
    fun toDomain(): PackageModel {
        return PackageModel(
            id = id,
            imageUrl = imageUrl,
            packageName = packageName,
            fileName = fileName,
            fileUrl = fileUrl,
            lastModifiedDate = lastModifiedDate,
            imageId = imageId,
            fileId = fileId
        )
    }
}