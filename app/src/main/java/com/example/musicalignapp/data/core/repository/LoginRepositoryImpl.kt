package com.example.musicalignapp.data.core.repository

import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : LoginRepository {

    override suspend fun saveUserId(key: String, userId: String): Boolean {
        return sharedPreferences.saveUserId(key, userId)
    }
}