package com.example.musicalignapp.core.converters.jsonconverter

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.util.Objects
import javax.inject.Inject

class AlignmentResultToJsonConverter @Inject constructor(
    @ApplicationContext private val context: Context,
): JsonConverter {

    private lateinit var uri: Uri
    private lateinit var jsonContent: String
    private lateinit var packageId: String

    override fun createJsonFile(jsonContent: String, packageId: String) : Uri {
        this.jsonContent = jsonContent
        this.packageId = packageId
        generateUri()

        return uri
    }

    override fun getJsonContent() {

    }

    private fun generateUri() {
        uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.musicalignapp.provider",
            createFile()
        )
    }

    private fun createFile(): File {
        val file = File.createTempFile(packageId, ".json", context.externalCacheDir)

        FileWriter(file).use { writer ->
            writer.write(jsonContent)
        }

        return file
    }
}