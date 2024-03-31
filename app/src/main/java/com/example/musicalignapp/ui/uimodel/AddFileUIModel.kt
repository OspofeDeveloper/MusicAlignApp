package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.ImageModel

data class AddFileUIModel(
    val packageName: String = "",
    val storageFile: FileUIModel = FileUIModel("", "", ""),
    val storageImage: ImageModel = ImageModel("", ""),
    val isImageUploading: Boolean = false,
    val isImageDeleting: Boolean = false,
    val isFileUploading: Boolean = false,
    val isFileDeleting: Boolean = false,
    val isPackageLoading: Boolean = false,
    val error: String? = null
) {
    fun isValidPackage() =
        storageImage.imageUri.isNotBlank() && storageFile.fileUri.isNotBlank() && packageName.isNotBlank() && storageFile.fileName.isNotBlank()
}