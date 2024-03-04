package com.example.musicalignapp.domain.usecases

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.musicalignapp.data.network.DataBaseService
import com.example.musicalignapp.domain.model.AlignmentModel
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.util.Objects
import javax.inject.Inject

class SaveAlignmentResultsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DataBaseService
) {

    private lateinit var uri: Uri
    private lateinit var json: String

    suspend operator fun invoke(alignmentResults: AlignmentModel): Boolean {
        val gson = Gson()
        json = gson.toJson(alignmentResults)
        generateUri(alignmentResults.packageId)
        return repository.uploadJsonFile(uri)
    }

    private fun generateUri(packageId: String) {
        uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.musicalignapp.provider",
            createFile(packageId)
        )
    }

    private fun createFile(packageId: String): File {
        val file = File.createTempFile("${packageId}_json", ".json", context.externalCacheDir)

        FileWriter(file).use { writer ->
            writer.write(json)
        }

        return file
    }

}