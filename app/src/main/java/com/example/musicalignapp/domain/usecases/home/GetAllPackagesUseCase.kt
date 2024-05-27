package com.example.musicalignapp.domain.usecases.home

import com.example.musicalignapp.data.remote.core.ApiResult
import com.example.musicalignapp.data.remote.core.error
import com.example.musicalignapp.data.remote.core.success
import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.domain.repository.HomeRepository
import com.example.musicalignapp.ui.uimodel.HomeUIModel
import javax.inject.Inject

class GetAllPackagesUseCase @Inject constructor(
  private val repository: HomeRepository
) {

    suspend operator fun invoke(userId: String) : ApiResult<HomeUIModel> {
        val response = repository.getAllPackages(userId)
        response.data?.let {
            return HomeUIModel(packages = it).success()
        }
        return response.error!!.errorResourceMessage(
            /*TODO override http codes -> mapOf(320 to R.string.custom_message_error)*/
        ).error()
    }
}