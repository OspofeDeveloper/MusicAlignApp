package com.example.musicalignapp.domain.usecases.home

import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.data.remote.firebase.DataBaseService
import com.example.musicalignapp.domain.repository.HomeRepository
import javax.inject.Inject

class DeletePackageUseCase @Inject constructor(
    private val repository: HomeRepository
) {

    suspend operator fun invoke(packageId: String): Boolean {
        return repository.deletePackage(packageId) &&
                repository.deleteFile(packageId) &&
                repository.deleteImage("$packageId.") &&
                repository.deleteJson(packageId)
    }
}