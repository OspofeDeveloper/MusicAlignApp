package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.remote.dto.ProjectDto

data class ProjectModel(
    val projectName: String = "",
    val imagesList: List<ImageModel> = emptyList(),
    val filesList: List<FileModel> = emptyList(),
    val jsonList: List<JsonModel> = emptyList(),
    val isFinished: Boolean = false,
    val lastModified: String = "",
    val originalImageUrl: String = "",
    val currentSystem: String  = "",
    val maxNumSystems: String = ""
) {
    fun toDto(): ProjectDto {
        return ProjectDto(
            project_name = projectName,
            imagesList = imagesList.map { it.toDto() },
            filesList = filesList.map { it.toDto() },
            jsonList = jsonList.map { it.toDto() },
            isFinished = isFinished,
            last_modified = lastModified,
            originalImageUrl = originalImageUrl,
            currentSystem = currentSystem,
            maxNumSystems = maxNumSystems
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