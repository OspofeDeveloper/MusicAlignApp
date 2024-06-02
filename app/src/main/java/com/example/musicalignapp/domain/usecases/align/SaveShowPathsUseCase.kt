package com.example.musicalignapp.domain.usecases.align

import androidx.compose.ui.unit.Constraints
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.domain.repository.AlignRepository
import javax.inject.Inject

class SaveShowPathsUseCase @Inject constructor(
    private val alignRepository: AlignRepository
){
    suspend operator fun invoke(showPaths: Boolean) {
        alignRepository.saveShowPaths(showPaths, Constants.SHOW_PATHS_KEY)
    }
}