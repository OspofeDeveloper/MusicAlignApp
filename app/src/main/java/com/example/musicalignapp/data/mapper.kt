package com.example.musicalignapp.data

import com.example.musicalignapp.data.remote.dto.SVGRequestDto
import com.example.musicalignapp.domain.model.SVGRequestModel

fun SVGRequestModel.toDto(): SVGRequestDto {
    return SVGRequestDto(
        fileName = fileName
    )
}