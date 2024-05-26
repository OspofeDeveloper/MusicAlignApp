package com.example.musicalignapp.ui.screens.addfile.viewmodel

import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel

data class ProjectUIModel(
    val projectName: String = "", //TOM.GLO
    val imagesList: List<ImageUIModel> = emptyList(), //TOM.GLO.01.jpg, TOM.GLO.02.jpg, TOM.GLO.03.jpg
    val filesList: List<FileUIModel> = emptyList(), //TOM.GLO.01.xml, TOM.GLO.02.xml, TOM.GLO.03.xml
    val jsonIdsList: List<String> = emptyList(),
    val isFinished: Boolean = false,
    val lastModified: String = "",
    val originalImageUrl: String = ""
) {
    fun isValidPackage() : Boolean {
        return imagesList.isNotEmpty() && filesList.isNotEmpty() && projectName.isNotBlank()
    }

    fun toDomain(): ProjectModel {
        return ProjectModel(
            projectName = projectName,
            imagesList = imagesList.map { it.toDomain() },
            filesList = filesList.map { it.toDomain() },
            jsonList = jsonIdsList.map { JsonModel(projectName, it) },
            isFinished = isFinished,
            lastModified = lastModified,
            originalImageUrl = originalImageUrl
        )
    }
}