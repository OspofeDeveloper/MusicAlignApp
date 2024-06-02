package com.example.musicalignapp.data.local.shared_prefs

import android.content.Context
import com.example.musicalignapp.core.Constants.PREFERENCES_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SharedPreferences {

    val storage: android.content.SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0)

    override suspend fun saveUserId(key: String, value: String): Boolean {
        storage.edit().putString(key, value).apply()
        return getUserId(key) == value
    }

    override suspend fun getUserId(key: String): String {
        return storage.getString(key, "")!!
    }

    override suspend fun saveAlignPathsToShow(key: String, value: Int): Boolean {
        storage.edit().putInt(key, value).apply()
        return getAlignPathsToShow(key) == value
    }

    override suspend fun getAlignPathsToShow(key: String): Int {
        return storage.getInt(key, 1)
    }

    override suspend fun saveShowPaths(key: String, value: Boolean): Boolean {
        storage.edit().putBoolean(key, value).apply()
        return getShowPaths(key) == value
    }

    override suspend fun getShowPaths(key: String): Boolean {
        return storage.getBoolean(key, false)
    }
}