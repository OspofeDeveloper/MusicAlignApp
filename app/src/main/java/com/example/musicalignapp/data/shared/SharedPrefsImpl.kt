package com.example.musicalignapp.data.shared

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicalignapp.data.shared.SharedPrefs.Companion.SHARED_PREFS_KEY_LIST_COORDINATES
import com.example.musicalignapp.data.shared.SharedPrefs.Companion.SHARED_PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = SHARED_PREFS_NAME)

class SharedPrefsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SharedPrefs {

    override suspend fun saveDrawCoordinates(listCoordinates: String) {
        val preferencesKey = stringPreferencesKey(SHARED_PREFS_KEY_LIST_COORDINATES)
        context.dataStore.edit {
            it[preferencesKey] = listCoordinates
        }
    }

    override suspend fun getDrawCoordinates(): Flow<String?> {
        return context.dataStore.data.map {
            it[stringPreferencesKey(SHARED_PREFS_KEY_LIST_COORDINATES)]
        }
    }

}