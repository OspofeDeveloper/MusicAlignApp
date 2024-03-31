package com.example.musicalignapp.data.remote.core

import android.util.Log

inline fun <T> tryCall(action: () -> T): ApiResult<T> = try {
    action().success()
} catch (e: Exception) {
    Log.e("NETWORK", e.toString())
    e.toError().error()
}