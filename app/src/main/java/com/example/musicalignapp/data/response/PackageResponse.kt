package com.example.musicalignapp.data.response

import com.example.musicalignapp.domain.model.PackageModel

data class PackageResponse(
    val id: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val fileUrl: String = "",
    val lastModifiedDate: String = ""
) {
    fun toDomain(): PackageModel {
        return PackageModel(
            id = id,
            imageUrl = imageUrl,
            name = name,
            fileUrl = fileUrl,
            lastModifiedDate = lastModifiedDate
        )
    }
}