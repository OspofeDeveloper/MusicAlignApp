package com.example.musicalignapp.ui.screens.replace_system.viewmodel

import android.net.Uri
import com.example.musicalignapp.domain.model.ReplaceSystemModel

data class ReplaceSystemState(
    val loading: Boolean = false,
    val error: String = "",
    val fileName: String = "",
    val fileUri: Uri = Uri.EMPTY,
    val imageName: String = "",
    val imageUri: Uri = Uri.EMPTY,
) {
    fun isValidPackage(): Boolean {
        return (fileName.isNotBlank() && fileUri.toString().isNotBlank()) ||
                (imageName.isNotBlank() && imageUri.toString().isNotBlank())
    }

    fun toDomain(): ReplaceSystemModel {
        return ReplaceSystemModel(
            fileName = fileName,
            fileUri = fileUri,
            imageName = imageName,
            imageUri = imageUri,
        )
    }
}