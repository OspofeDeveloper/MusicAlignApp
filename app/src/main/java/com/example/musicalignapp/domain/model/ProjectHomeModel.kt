package com.example.musicalignapp.domain.model

data class ProjectHomeModel(
    val projectName: String,
    val currentSystem: String,
    val originalImageUrl: String,
    val lastModified: String,
    val isFinished: Boolean
)
