package com.example.musicalignapp.data.remote.api

import com.example.musicalignapp.data.core.NetworkError
import com.example.musicalignapp.data.core.tryCallContent
import com.example.musicalignapp.data.core.tryCallNames
import com.example.musicalignapp.data.remote.dto.SVGRequestDto
import com.example.musicalignapp.data.remote.dto.SVGResponseDto
import com.example.musicalignapp.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class ApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun requestSvgsFromImage(requestDto: SVGRequestDto): Result<SVGResponseDto, NetworkError> {
        return tryCallNames {
            httpClient.post("/partitures/request.php") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }
    }

    suspend fun getSvgContent(requestDto: SVGRequestDto): Result<String, NetworkError> {
        return tryCallContent {
            httpClient.post("/partitures/get.php") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }
    }
}