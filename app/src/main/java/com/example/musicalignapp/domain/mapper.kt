package com.example.musicalignapp.domain

import com.example.musicalignapp.data.remote.dto.SVGRequestDto
import com.example.musicalignapp.data.remote.dto.SVGResponseDto
import com.example.musicalignapp.domain.model.SVGRequestModel
import com.example.musicalignapp.domain.model.SVGResponseModel

fun SVGRequestDto.toDomain(): SVGRequestModel {
    return SVGRequestModel(
        fileName = fileName
    )
}

fun SVGResponseDto.toDomain(): SVGResponseModel {
    return SVGResponseModel(
        files = files ?: emptyList()
    )
}