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
}