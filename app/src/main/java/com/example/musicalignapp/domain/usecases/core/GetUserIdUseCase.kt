package com.example.musicalignapp.domain.usecases.core

import com.example.musicalignapp.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val repository: HomeRepository
) {

    suspend operator fun invoke(key: String): String {
        return repository.getUserId(key)
    }
}