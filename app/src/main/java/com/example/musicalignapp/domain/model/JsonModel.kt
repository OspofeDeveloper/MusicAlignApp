package com.example.musicalignapp.domain.model

import android.net.Uri
import androidx.core.net.toUri
import com.example.musicalignapp.data.remote.dto.JsonDto

data class JsonModel(
    val jsonProjectName: String,
    val jsonId: String,
    val jsonUri: Uri = "".toUri()
) {
    fun toDto(): JsonDto {
        return JsonDto(
            jsonProjectName = jsonProjectName,
            jsonId = jsonId,
            jsonUri = jsonUri
        )
    }
}
