package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.ui.uimodel.finaloutput.Image
import java.util.Date

data class ImageUIModel (
    val id: String,
    val imageUri: String,
    val height: Int = 0,
    val width: Int = 0,
    val originX: Int = 0,
    val originY: Int = 0,
) {
    fun toDomain(): ImageModel {
        return ImageModel(
            imageName = id,
            imageUrl = imageUri
        )
    }

    fun toFinalOutputImage(isOriginal: Boolean): Image {
        return Image(
            id = if (isOriginal) 0 else this.id.substringBeforeLast(".").substringAfterLast(".").toInt(),
            width = this.width,
            height = this.height,
            fileName = this.imageUri,
            license = 1,
            flickrUrl = "",
            cocoUrl = "",
            dateCaptured = Date(),
            originX = this.originX,
            originY = this.originY
        )
    }
}