package com.example.musicalignapp.ui.uimodel.finaloutput

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Info(
    val year: Int = 0,
    val version: String = "",
    val description: String = "",
    val contributor: String = "",
    val url: String = "",
    @SerializedName("date_created")
    val dateCreated: Date = Date(),
)