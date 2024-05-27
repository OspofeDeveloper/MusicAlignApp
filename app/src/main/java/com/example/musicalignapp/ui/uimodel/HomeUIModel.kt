package com.example.musicalignapp.ui.uimodel

import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.domain.model.ProjectModel

data class HomeUIModel(
    val packages: List<ProjectHomeModel> = emptyList(),
)