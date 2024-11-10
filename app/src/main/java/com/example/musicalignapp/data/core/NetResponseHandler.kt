package com.example.musicalignapp.data.core

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import com.example.musicalignapp.utils.Result as Result

fun Exception.toNetError(): NetworkError = when (this) {
    is UnresolvedAddressException -> NetworkError.NO_INTERNET
    is SerializationException -> NetworkError.SERIALIZATION
    is JsonConvertException -> NetworkError.SERIALIZATION
    else -> NetworkError.UNKNOWN
}

suspend inline fun <reified T> handleResponse(response: HttpResponse): Result<T, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body())
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