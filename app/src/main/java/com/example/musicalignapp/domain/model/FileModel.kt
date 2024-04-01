package com.example.musicalignapp.domain.model

import com.example.musicalignapp.ui.uimodel.FileUIModel

data class FileModel (
    val id: String,
    val fileUri: String
) {
    fun toUIModel(fileName: String) : FileUIModel {
        return FileUIModel(
            id = id,
            fileUri = fileUri,
            fileName = fileName
        )
    }
}