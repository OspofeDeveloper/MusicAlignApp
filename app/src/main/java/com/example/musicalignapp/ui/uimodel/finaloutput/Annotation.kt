package com.example.musicalignapp.ui.uimodel.finaloutput


data class Annotation(
    val id: String,
    val imageId: Int,
    val categoryId: Int,
    val segmentation: List<List<Int>>,
    val area: Double,
    val bbox: List<Int>,
    val iscrowd: Int = 0,
)