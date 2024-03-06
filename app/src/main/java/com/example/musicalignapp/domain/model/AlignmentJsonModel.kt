package com.example.musicalignapp.domain.model

data class AlignmentJsonModel (
    val packageId: String,
    val alignmentElements: List<AlignmentElements>
)

data class AlignmentElements (
    val elementId: String
)