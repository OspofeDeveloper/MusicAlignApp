package com.example.musicalignapp.ui.uimodel

data class AlignmentDataUIModel(
    val fileContent: String,
    val listElements: List<Map<String, String>>,
    val imageUri: String
)
