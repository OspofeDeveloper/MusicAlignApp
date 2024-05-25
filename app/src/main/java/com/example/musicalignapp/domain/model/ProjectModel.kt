package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.remote.dto.ProjectDto

data class ProjectModel(
    val projectName: String,
    val imagesList: List<ImageModel>,
    val filesList: List<FileModel>,
    val jsonList: List<JsonModel>,
    val isFinished: Boolean,
    val lastModified: String
) {
    fun toDto(): ProjectDto {
        return ProjectDto(
            projectName = projectName,
            imagesList = imagesList.map { it.toDto() },
            filesList = filesList.map { it.toDto() },
            jsonList = jsonList.map { it.toDto() },
            isFinished = isFinished,
            lastModified = lastModified
        )
    }
}
//data class PackageModel(
//    val id: String,
//    val imageUrl: String,
//    val packageName: String,
//    val fileName: String,
//    val fileUrl: String,
//    val lastModifiedDate: String,
//    val fileId: String,
//    val imageId: String,
//    var jsonId: String
//) {
//    fun toDto(): PackageDto {
//        return PackageDto(
//            id = id,
//            imageUrl = imageUrl,
//            packageName = packageName,
//            fileName = fileName,
//            fileUrl = fileUrl,
//            lastModifiedDate = lastModifiedDate,
//            imageId = imageId,
//            fileId = fileId,
//            jsonId = jsonId
//        )
//    }
//}