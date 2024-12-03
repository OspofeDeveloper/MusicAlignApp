package com.example.musicalignapp.data.core

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import com.example.musicalignapp.utils.Result as Result

fun Exception.toNetError(): NetworkError = when (this) {
    is UnresolvedAddressException -> NetworkError.NO_INTERNET
    is SerializationException -> NetworkError.SERIALIZATION
    is JsonConvertException -> NetworkError.SERIALIZATION
    else -> NetworkError.UNKNOWN
}

suspend inline fun <reified T> handleNamesResponse(response: HttpResponse): Result<T, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> {
            val responseText = response.bodyAsText()

            val json = Json { ignoreUnknownKeys = true }
            val finalResponse = json.decodeFromString<T>(responseText)

            Result.Success(finalResponse)
        }
        401 -> {
            Result.Error(NetworkError.UNAUTHORIZED)
        }
        409 -> Result.Error(NetworkError.CONFLICT)
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)
    }
}

suspend inline fun handleContentResponse(response: HttpResponse): Result<String, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> {
            val responseText = response.bodyAsText()
            Result.Success(responseText)
        }
        401 -> {
            Result.Error(NetworkError.UNAUTHORIZED)
        }
        409 -> Result.Error(NetworkError.CONFLICT)
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)
    }
}