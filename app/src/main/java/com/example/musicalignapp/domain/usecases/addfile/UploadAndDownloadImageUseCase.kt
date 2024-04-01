package com.example.musicalignapp.domain.usecases.addfile

import android.net.Uri
import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.error
import com.example.musicalignapp.data.remote.core.success
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import javax.inject.Inject

class UploadAndDownloadImageUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(uri: Uri): ApiResult<ImageUIModel> {
        val response = repository.uploadAndDownloadImage(uri)
        response.data?.let {
            return response.data.toUIModel().success()
        }
        return response.error!!.errorResourceMessage(
            /*TODO override http codes -> mapOf(320 to R.string.custom_message_error)*/
        ).error()
    }
}