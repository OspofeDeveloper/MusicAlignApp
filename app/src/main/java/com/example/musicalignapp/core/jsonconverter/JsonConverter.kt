package com.example.musicalignapp.core.jsonconverter

import android.net.Uri

interface JsonConverter {

    fun createJsonFile(jsonContent: String, packageId: String) : Uri

    fun getJsonContent()

}