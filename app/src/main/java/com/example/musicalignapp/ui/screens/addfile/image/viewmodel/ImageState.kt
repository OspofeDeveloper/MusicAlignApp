package com.example.musicalignapp.ui.screens.addfile.image.viewmodel

import com.example.musicalignapp.domain.model.ImageModel

data class ImageState (
    val isImageUploading: Boolean = false,
    val storageImage: ImageModel = ImageModel("", ""),
    val error: String? = null,
    val isImageDeleting: Boolean = false,
)