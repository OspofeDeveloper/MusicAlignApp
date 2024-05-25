package com.example.musicalignapp.data.remote.dto

import com.example.musicalignapp.domain.model.ImageModel

data class ImageDto (
    val imageNameNoExtension: String,
    val imageName: String,
    val imageUrl: String,
) {
    fun toDomain(): ImageModel {
        return ImageModel(
            imageName = imageName,
            imageUrl = imageUrl
        )
    }
}