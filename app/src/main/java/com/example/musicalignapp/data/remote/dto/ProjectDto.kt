package com.example.musicalignapp.data.remote.dto

import com.example.musicalignapp.domain.model.ProjectModel

data class ProjectDto(
    val projectName: String = "",
    val imagesList: List<ImageDto> = emptyList(),
    val filesList: List<FileDto> = emptyList(),
    val jsonList: List<JsonDto> = emptyList(),
    val isFinished: Boolean,
    val lastModified: String,
    val originalImageUrl: String
) {
    fun toDomain(): ProjectModel {
        return ProjectModel(
            projectName = projectName,
            imagesList = imagesList.map { it.toDomain() },
            filesList = filesList.map { it.toDomain() },
            jsonList = jsonList.map { it.toDomain() },
            isFinished = isFinished,
            lastModified = lastModified,
            originalImageUrl = originalImageUrl
        )
    }
}
//data class PackageDto(
//    val id: String = "",
//    val imageUrl: String = "",
//    val packageName: String = "",
//    val fileName: String = "",
//    val fileUrl: String = "",
//    val lastModifiedDate: String = "",
//    val imageId: String = "",
//    val fileId: String = "",
//    val jsonId: String = ""
//) {
//    fun toDomain(): PackageModel {
//        return PackageModel(
//            id = id,
//            imageUrl = imageUrl,
//            packageName = packageName,
//            fileName = fileName,
//            fileUrl = fileUrl,
//            lastModifiedDate = lastModifiedDate,
//            imageId = imageId,
//            fileId = fileId,
//            jsonId = "${id}_json"
//        )
//    }
//}