package com.example.musicalignapp.data.remote.dto

import com.example.musicalignapp.domain.model.FileModel

data class FileDto (
    val fileNameNoExtension: String,
    val fileName: String,
    val fileUrl: String
) {
    fun toDomain(): FileModel {
        return FileModel(
            fileName = fileName,
            fileUrl = fileUrl
        )
    }
}