package com.example.musicalignapp.core.converters.jsonconverter

import android.net.Uri

interface JsonConverter {

    fun createJsonFile(jsonContent: String, packageId: String) : Uri

    fun createFinalOutputJsonFile(jsonContent: String, packageId: String) : Uri

    fun createSVGFile(svgContent: String, fileName: String) : Uri
}