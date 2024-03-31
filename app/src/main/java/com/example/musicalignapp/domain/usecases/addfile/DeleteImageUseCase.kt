package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(imageId: String) : Boolean {
        return repository.deleteImage(imageId)
    }
}