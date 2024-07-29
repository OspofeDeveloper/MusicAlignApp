package com.example.musicalignapp.domain.model

data class AlignmentJsonModel (
    val packageId: String,
    val systemId: String,
    val lastElementId: String,
    val highestElementId: String,
    val alignmentElements: List<Map<String, String>>,
)