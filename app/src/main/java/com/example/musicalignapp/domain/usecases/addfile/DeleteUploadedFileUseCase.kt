package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class DeleteUploadedFileUseCase @Inject constructor(
    private val repository: AddFileRepository
) {

    suspend operator fun invoke(fileId: String) : Boolean {
        return repository.deleteFile(fileId)
    }

}