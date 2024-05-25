package com.example.musicalignapp.domain.model

import com.example.musicalignapp.data.remote.dto.ImageDto
import com.example.musicalignapp.ui.uimodel.ImageUIModel

data class ImageModel (
    val imageName: String,
    val imageUrl: String
) {
    fun toUIModel() : ImageUIModel {
        return ImageUIModel(
            id = imageName,
            imageUri = imageUrl
        )
    }

    fun toDto(): ImageDto {
        return ImageDto(
            imageNameNoExtension = imageName.substringBeforeLast('.'),
            imageName = imageName,
            imageUrl = imageUrl
        )
    }
}