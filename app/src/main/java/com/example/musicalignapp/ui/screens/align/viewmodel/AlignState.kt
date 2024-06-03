package com.example.musicalignapp.ui.screens.align.viewmodel

import com.example.musicalignapp.core.extensions.toTwoDigits
import java.io.File

data class AlignState(
    val systemNumber: String = 0.toTwoDigits(),
    val initDrawCoordinates: String = "",
    val file: String = "",
    val alignedElements: MutableList<AlignedElement> = mutableListOf(),
    val maxSystemNumber: String = "",
    val lastElementId: String = "",
    val highestElementId: String = "",
    val listElementIds: List<String> = emptyList(),
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: Boolean = false,
    val isElementAligned: Boolean = false,
    val pathsToDraw: Int = 0
)