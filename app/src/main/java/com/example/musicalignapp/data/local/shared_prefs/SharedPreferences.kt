package com.example.musicalignapp.data.local.shared_prefs

interface SharedPreferences {

    suspend fun saveUserId(key: String, value: String): Boolean

    suspend fun getUserId(key: String): String
}