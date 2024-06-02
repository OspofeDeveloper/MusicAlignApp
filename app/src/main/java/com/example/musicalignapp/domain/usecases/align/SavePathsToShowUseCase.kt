package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants.PATHS_TO_SHOW_KEY
import com.example.musicalignapp.domain.repository.AlignRepository
import javax.inject.Inject

class SavePathsToShowUseCase @Inject constructor(
    private val alignRepository: AlignRepository
) {
    suspend operator fun invoke(pathsToShow: Int) {
        alignRepository.savePathsToShow(PATHS_TO_SHOW_KEY, pathsToShow)
    }
}