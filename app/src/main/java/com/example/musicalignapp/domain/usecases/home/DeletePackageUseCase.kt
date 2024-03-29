package com.example.musicalignapp.domain.usecases.home

import com.example.musicalignapp.data.network.DataBaseService
import javax.inject.Inject

class DeletePackageUseCase @Inject constructor(
    private val repository: DataBaseService
) {

    suspend operator fun invoke(
        packageId: String, fileId: String, imageId: String, jsonId: String
    ): Boolean {
        return repository.deletePackage(packageId) &&
                repository.deleteFile(fileId) &&
                repository.deleteImage(imageId) &&
                repository.deleteJson(jsonId)
    }
}