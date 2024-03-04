package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.AlignmentElements
import com.example.musicalignapp.domain.model.AlignmentModel

data class AlignmentUIModel (
    val packageId: String,
    val elementIds: List<String>
    //val coordinates: List<String>
) {
    fun toDomain(): AlignmentModel {
        return AlignmentModel(
            packageId = packageId,
            alignmentElements = elementIds.map { AlignmentElements(it) }
        )
    }
}