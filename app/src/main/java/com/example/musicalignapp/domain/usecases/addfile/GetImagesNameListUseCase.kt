package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.domain.repository.AddFileRepository
import javax.inject.Inject

class GetImagesNameListUseCase @Inject constructor(
    private val addFileRepository: AddFileRepository
) {

    suspend operator fun invoke(): List<String> {
        return addFileRepository.getImagesNameList()
    }
}