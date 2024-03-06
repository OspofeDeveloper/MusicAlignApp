package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.AlignmentElements
import com.example.musicalignapp.domain.model.AlignmentJsonModel

data class AlignmentJsonUIModel(
    val packageId: String,
    val elementIds: List<String>
    //val coordinates: List<String>
) {
    fun toDomain(): AlignmentJsonModel {
        return AlignmentJsonModel(
            packageId = packageId,
            alignmentElements = elementIds.map { AlignmentElements(it) }
        )
    }
}