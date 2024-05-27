package com.example.musicalignapp.data.remote.dto

import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.domain.model.ProjectModel
import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val project_name: String = "",
    val imagesList: List<ImageDto> = emptyList(),
    val filesList: List<FileDto> = emptyList(),
    val jsonList: List<JsonDto> = emptyList(),
    val isFinished: Boolean = false,
    val last_modified: String = "",
    val originalImageUrl: String = "",
) {
    fun toDomain(): ProjectModel {
        return ProjectModel(
            projectName = project_name,
            imagesList = imagesList.map { it.toDomain() },
            filesList = filesList.map { it.toDomain() },
            jsonList = jsonList.map { it.toDomain() },
            isFinished = isFinished,
            lastModified = last_modified,
            originalImageUrl = originalImageUrl
        )
    }

    fun toProjectHomeModel(): ProjectHomeModel {
        return ProjectHomeModel(
            projectName = project_name,
            originalImageUrl = originalImageUrl,
            lastModified = last_modified,
            isFinished = isFinished
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