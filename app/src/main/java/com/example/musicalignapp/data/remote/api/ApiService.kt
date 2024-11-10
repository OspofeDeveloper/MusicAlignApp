package com.example.musicalignapp.data.remote.api

import com.example.musicalignapp.data.core.NetworkError
import com.example.musicalignapp.data.core.tryCall
import com.example.musicalignapp.data.remote.dto.SVGRequestDto
import com.example.musicalignapp.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class ApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun requestSvgsFromImage(requestDto: SVGRequestDto): Result<Unit, NetworkError> {
        return tryCall {
            httpClient.post("/partitures/request.php") {
                 setBody(requestDto)
            }
        }
    }

    suspend fun getSvgContent(requestDto: SVGRequestDto): Result<String, NetworkError> {
        return tryCall {
            httpClient.post("/partitures/get.php") {
                setBody(requestDto)
            }
        }
    }
}