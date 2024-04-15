package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.AlignmentJsonModel

data class AlignmentJsonUIModel(
    val packageId: String,
    val elementIds: List<Map<String, String>>,
    val elementsStrokes: List<Map<String, String>>
) {
    fun toDomain(): AlignmentJsonModel {
        val newElementIds = elementIds.toMutableList()
        newElementIds.removeIf { it == mapOf("" to "") }

        val newStrokes = elementsStrokes.toMutableList()
        newStrokes.removeIf { it == mapOf("" to "") }

        return AlignmentJsonModel(
            packageId = packageId,
            alignmentElements = newElementIds,
            alignmentElementsStroke = elementsStrokes
        )
    }
}