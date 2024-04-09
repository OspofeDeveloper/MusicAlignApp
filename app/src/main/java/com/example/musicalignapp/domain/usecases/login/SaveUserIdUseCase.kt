package com.example.musicalignapp.domain.usecases.login

import com.example.musicalignapp.domain.repository.LoginRepository
import com.example.musicalignapp.domain.usecases.core.GetUserIdUseCase
import javax.inject.Inject

class SaveUserIdUseCase @Inject constructor(
    private val repository: LoginRepository
) {

    suspend operator fun invoke(key: String, userId: String): Boolean {
        return repository.saveUserId(key, userId)
    }
}