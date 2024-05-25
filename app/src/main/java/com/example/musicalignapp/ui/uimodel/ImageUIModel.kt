package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.ImageModel

data class ImageUIModel (
    val id: String,
    val imageUri: String
) {
    fun toDomain(): ImageModel {
        return ImageModel(
            imageName = id,
            imageUrl = imageUri
        )
    }
}