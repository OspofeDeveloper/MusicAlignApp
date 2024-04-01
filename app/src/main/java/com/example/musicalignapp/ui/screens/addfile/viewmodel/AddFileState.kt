package com.example.musicalignapp.ui.screens.addfile.viewmodel

import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel

data class AddFileUIModel(
    val packageName: String = "",
    val image: ImageUIModel = ImageUIModel("", ""),
    val file: FileUIModel = FileUIModel("", "", ""),
    val jsonId: String = ""
) {
    fun isValidPackage() =
        image.imageUri.isNotBlank() && packageName.isNotBlank() && file.fileName.isNotBlank() && file.fileUri.isNotBlank()

    fun toDomain(projectId: String, projectDate: String): PackageModel {
        return PackageModel(
            id = projectId,
            imageUrl = image.imageUri,
            fileId = file.id,
            fileName = file.fileName,
            fileUrl = file.fileUri,
            imageId = image.id,
            jsonId = jsonId,
            lastModifiedDate = projectDate,
            packageName = packageName
        )
    }
}