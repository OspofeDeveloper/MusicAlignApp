package com.example.musicalignapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SVGResponseDto(
    @SerialName("files")
    val files: List<String>?,
)
