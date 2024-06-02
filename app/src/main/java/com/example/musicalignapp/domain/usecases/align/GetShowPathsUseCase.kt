package com.example.musicalignapp.domain.usecases.align

import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.domain.repository.AlignRepository
import javax.inject.Inject

class GetShowPathsUseCase @Inject constructor(
    private val alignRepository: AlignRepository
){
    suspend operator fun invoke() : Boolean {
        return alignRepository.getShowPaths(Constants.SHOW_PATHS_KEY)
    }
}