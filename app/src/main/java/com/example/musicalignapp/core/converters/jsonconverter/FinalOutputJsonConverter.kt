package com.example.musicalignapp.core.converters.jsonconverter

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.util.Objects
import javax.inject.Inject

class FinalOutputJsonConverter @Inject constructor(
    @ApplicationContext private val context: Context,
) : JsonConverter {
    private lateinit var uri: Uri
    private lateinit var jsonContent: String
    private lateinit var jsonName: String

    override fun createJsonFile(jsonContent: String, packageId: String): Uri {
        return "".toUri()
    }

    override fun createFinalOutputJsonFile(jsonContent: String, jsonName: String): Uri {
        this.jsonContent = jsonContent
        this.jsonName = jsonName
        generateUri()

        return uri
    }

    private fun generateUri() {
        uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.musicalignapp.provider",
            createFile()
        )
    }

    private fun createFile(): File {
        val file = File.createTempFile(jsonName, ".json", context.externalCacheDir)

        FileWriter(file).use { writer ->
            writer.write(jsonContent)
        }

        return file
    }
}