package com.example.musicalignapp.ui.screens.align.viewmodel

data class AlignState(
    val initDrawCoordinates: String = "",
    val fileContent: String = "",
    val alignedElements: MutableList<AlignedElement> = mutableListOf(),
    val alignedElementsStrokes: MutableList<AlignedStroke> = mutableListOf(),
    val listElementIds: List<String> = emptyList(),
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: Boolean = false,
    val isElementAligned: Boolean = false
)