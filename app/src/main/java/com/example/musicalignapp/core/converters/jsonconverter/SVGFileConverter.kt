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

class SVGFileConverter @Inject constructor(
    @ApplicationContext private val context: Context,
) : JsonConverter{

    private lateinit var uri: Uri
    private lateinit var svgContent: String
    private lateinit var fileName: String

    override fun createJsonFile(jsonContent: String, packageId: String): Uri {
        return "".toUri()
    }

    override fun createFinalOutputJsonFile(jsonContent: String, packageId: String): Uri {
        return "".toUri()
    }

    override fun createSVGFile(svgContent: String, fileName: String): Uri {
        this.svgContent = svgContent
        this.fileName = fileName
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
        val file = File.createTempFile(fileName, ".svg", context.externalCacheDir)

        FileWriter(file).use { writer ->
            writer.write(svgContent)
        }

        return file
    }
}
