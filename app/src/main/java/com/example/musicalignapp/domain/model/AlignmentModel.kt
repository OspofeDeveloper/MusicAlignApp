package com.example.musicalignapp.domain.model

data class AlignmentModel (
    val packageId: String,
    val alignmentElements: List<AlignmentElements>
)

data class AlignmentElements (
    val elementId: String
)