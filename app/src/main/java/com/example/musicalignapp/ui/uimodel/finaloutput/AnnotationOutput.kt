package com.example.musicalignapp.ui.uimodel.finaloutput


data class AnnotationOutput(
    val id: String,
    val imageId: Int,
    val categoryId: Int,
    val segmentation: List<Int>,
    val area: Int,
    val bbox: List<Int>,
    val iscrowd: Int = 0,
)