package com.example.musicalignapp.ui.uimodel

data class AlignmentDataUIModel(
    val fileContent: String,
    val listElements: List<Map<String, String>>,
    val listElementStrokes: List<Map<String, String>>,
    val lastElementId: String,
    val highestElementId: String,
    val imageUri: String
)
