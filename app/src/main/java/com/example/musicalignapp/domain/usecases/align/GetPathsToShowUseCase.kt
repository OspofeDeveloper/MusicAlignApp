package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants.PATHS_TO_SHOW_KEY
import com.example.musicalignapp.domain.repository.AlignRepository
import javax.inject.Inject

class GetPathsToShowUseCase @Inject constructor(
    private val alignRepository: AlignRepository
) {

    suspend operator fun invoke(): Int {
        alignRepository.getPathsToShow(PATHS_TO_SHOW_KEY).also {
            return if (it == -1)  1 else it
        }
    }

}