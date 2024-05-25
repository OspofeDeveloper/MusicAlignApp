package com.example.musicalignapp.data.remote.dto

import android.net.Uri
import com.example.musicalignapp.domain.model.JsonModel

data class JsonDto(
    val jsonProjectName: String,
    val jsonId: String,
    val jsonUri: Uri
) {
    fun toDomain(): JsonModel {
        return JsonModel(
            jsonProjectName = jsonProjectName,
            jsonId = jsonId,
            jsonUri = jsonUri
        )
    }
}
