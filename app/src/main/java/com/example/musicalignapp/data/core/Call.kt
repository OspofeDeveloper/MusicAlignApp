package com.example.musicalignapp.data.core

import android.util.Log
import com.example.musicalignapp.data.remote.dto.SVGResponseDto
import io.ktor.client.statement.HttpResponse
import com.example.musicalignapp.utils.Result as Result

suspend inline fun tryCallNames(action: () -> HttpResponse): Result<SVGResponseDto, NetworkError> {
    return try {
        val response = action()
        handleNamesResponse(response)
    }
    catch (e: Exception) {
        Log.w("NET_ERROR", e.toString())
        Result.Error(e.toNetError())
    }
}

suspend inline fun tryCallContent(action: () -> HttpResponse): Result<String, NetworkError> {
    return try {
        val response = action()
        handleContentResponse(response)
    }
    catch (e: Exception) {
        Log.w("NET_ERROR", e.toString())
        Result.Error(e.toNetError())
    }
}