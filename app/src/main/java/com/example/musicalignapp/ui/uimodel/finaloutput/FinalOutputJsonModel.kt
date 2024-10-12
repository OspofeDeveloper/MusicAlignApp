package com.example.musicalignapp.ui.uimodel.finaloutput

import com.example.musicalignapp.data.local.categories.Categories

data class FinalOutputJsonModel(
    val info: Info,
    val images: List<Image> = emptyList(),
    val annotations: List<AnnotationOutput> = emptyList(),
    val licenses: List<License>,
    val categories: List<Category> = Categories.listCategories,
    val isFinished: Boolean = false,
    val isFinishedGood: Boolean = false,
    val projectDuration: String = "00:00:00",
)