package com.example.musicalignapp.data.local.shared_prefs

interface SharedPreferences {

    suspend fun saveUserId(key: String, value: String): Boolean

    suspend fun getUserId(key: String): String

    suspend fun saveUserEmail(key: String, value: String): Boolean

    suspend fun getUserEmail(key: String): String

    suspend fun saveAlignPathsToShow(key: String, value: Int): Boolean

    suspend fun getAlignPathsToShow(key: String): Int

    suspend fun saveShowPaths(key: String, value: Boolean): Boolean

    suspend fun getShowPaths(key: String): Boolean
}