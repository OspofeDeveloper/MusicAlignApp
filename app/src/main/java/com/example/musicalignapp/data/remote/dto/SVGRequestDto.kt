package com.example.musicalignapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SVGRequestDto (
    @SerialName("filename")
    val fileName: String,
)