package com.example.musicalignapp.data.shared

import kotlinx.coroutines.flow.Flow

interface SharedPrefs {

    companion object {
        const val SHARED_PREFS_NAME = "SHARED_PREFS_NAME"
        const val SHARED_PREFS_KEY_LIST_COORDINATES = "SHARED_PREFS_KEY_LIST_COORDINATES"
    }

    suspend fun saveDrawCoordinates(listCoordinates: String)

    suspend fun getDrawCoordinates(): Flow<String?>
}