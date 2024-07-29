package com.example.musicalignapp.ui.uimodel.finaloutput

data class Image(
    val id: Int,
    val width: Int,
    val height: Int,
    val fileName: String,
    val license: Int = 1,
    val flickrUrl: String = "",
    val cocoUrl: String = "",
    val dateCaptured: String = "",
    val originX: Int,
    val originY: Int,
)