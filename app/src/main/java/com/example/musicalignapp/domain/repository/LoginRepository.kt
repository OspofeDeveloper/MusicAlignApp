package com.example.musicalignapp.domain.repository

interface LoginRepository {

    suspend fun saveUserId(key: String, userId: String) : Boolean
}