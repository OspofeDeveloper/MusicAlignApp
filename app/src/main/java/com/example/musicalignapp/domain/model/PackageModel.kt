package com.example.musicalignapp.domain.model

data class PackageModel (
    val id: String,
    val imageUrl: String,
    val packageName: String,
    val fileName: String,
    val fileUrl: String,
    val lastModifiedDate: String
)