package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.PackageModel

data class HomeUIModel(
    val packages: List<PackageModel> = emptyList(),
    val lastModifiedPackages: List<PackageModel> = emptyList(),
)