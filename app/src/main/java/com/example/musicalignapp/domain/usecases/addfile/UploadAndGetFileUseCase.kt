package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.error
import com.example.musicalignapp.data.remote.core.success
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.ui.uimodel.FileUIModel
import javax.inject.Inject

class UploadAndGetFileUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(uri: Uri, fileName: String): ApiResult<FileUIModel> {
        val response = repository.uploadAndGetFile(uri, fileName)
        response.data?.let {
            if (it.id.isNotBlank() && it.fileUri.isNotBlank()) {
                return response.data.toUIModel(fileName).success()
            }
        }
        return response.error!!.errorResourceMessage(
            /*TODO override http codes -> mapOf(320 to R.string.custom_message_error)*/
        ).error()
    }
}