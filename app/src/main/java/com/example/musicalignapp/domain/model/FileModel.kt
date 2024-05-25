package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.remote.dto.FileDto
import com.example.musicalignapp.ui.uimodel.FileUIModel

data class FileModel (
    val fileName: String,
    val fileUrl: String
) {
    fun toUIModel(fileName: String) : FileUIModel {
        return FileUIModel(
            id = fileName,
            fileUri = fileUrl,
            fileName = fileName
        )
    }

    fun toDto(): FileDto {
        return FileDto(
            fileNameNoExtension = fileName.substringBeforeLast('.'),
            fileName = fileName,
            fileUrl = fileUrl
        )
    }
}