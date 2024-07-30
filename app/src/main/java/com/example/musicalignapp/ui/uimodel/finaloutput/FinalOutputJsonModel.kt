package com.example.musicalignapp.ui.uimodel.finaloutput

data class FinalOutputJsonModel(
    val info: Info,
    val images: List<Image> = emptyList(),
    val annotations: List<Annotation> = emptyList(),
    val licenses: List<License>,
    val categories: List<Category> = emptyList(),
)