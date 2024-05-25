package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.FileModel

data class FileUIModel (
    val id: String,
    val fileUri: String,
    val fileName: String
) {
    fun toDomain(): FileModel {
        return FileModel(
            fileName = fileName,
            fileUrl = fileUri
        )
    }
}