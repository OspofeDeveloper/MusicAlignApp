package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.AlignmentJsonModel

data class AlignmentJsonUIModel(
    val packageId: String,
    val systemId: String,
    val lastElementId: String,
    val highestElementId: String,
    val elementIds: List<Map<String, String>>,
) {
    fun toDomain(): AlignmentJsonModel {
        val newElementIds = elementIds.toMutableList()
        newElementIds.removeIf { it == mapOf("" to "") }

        return AlignmentJsonModel(
            packageId = packageId,
            systemId = systemId,
            lastElementId = lastElementId,
            highestElementId = highestElementId,
            alignmentElements = newElementIds,
        )
    }
}