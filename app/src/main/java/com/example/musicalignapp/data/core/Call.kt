package com.example.musicalignapp.data.core

import android.util.Log
import io.ktor.client.statement.HttpResponse
import com.example.musicalignapp.utils.Result as Result

suspend inline fun <reified T> tryCall(action: () -> HttpResponse): Result<T, NetworkError> {
    return try {
        val response = action()
        handleResponse(response)
    } catch (e: Exception) {
        Log.w("NET_ERROR", e.toString())
        Result.Error(e.toNetError())
    }
}