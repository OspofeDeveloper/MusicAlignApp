package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.ProjectModel

data class HomeUIModel(
    val packages: List<ProjectModel> = emptyList(),
    val lastModifiedPackages: List<ProjectModel> = emptyList(),
)