package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.ui.uimodel.finaloutput.FinalOutputJsonModel
import java.io.File

data class AlignmentDataUIModel(
    val file: String?,
    val currentSystem: String,
    val listElements: List<Map<String, String>>,
    val maxSystemNumber: String,
    val lastElementId: String,
    val highestElementId: String,
    val imageUri: String,
    val currentImageId: Int,
    val finalOutputJsonModel: FinalOutputJsonModel
)
