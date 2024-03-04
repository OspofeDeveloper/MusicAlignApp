package com.example.musicalignapp.domain.model

import com.example.musicalignapp.ui.uimodel.PackageUIModel

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
    fun toUIModel() : PackageUIModel {
        return PackageUIModel(
            id = id,
            imageUrl = imageUrl,
            packageName = packageName,
            fileName = fileName,
            fileUrl = fileUrl,
            lastModifiedDate = lastModifiedDate
        )
    }
}