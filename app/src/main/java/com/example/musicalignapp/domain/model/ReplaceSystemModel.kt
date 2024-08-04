package com.example.musicalignapp.domain.model

import android.net.Uri

data class ReplaceSystemModel(
    val fileName: String = "",
    val fileUri: Uri = Uri.EMPTY,
    val imageName: String = "",
    val imageUri: Uri = Uri.EMPTY,
)
