package com.example.musicalignapp.domain.model

import com.example.musicalignapp.ui.uimodel.ImageUIModel

data class ImageModel (
    val id: String,
    val imageUri: String
) {
    fun toUIModel() : ImageUIModel {
        return ImageUIModel(
            id = id,
            imageUri = imageUri
        )
    }
}