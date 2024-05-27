package com.example.musicalignapp.data.remote.core

import com.example.musicalignapp.domain.model.ProjectHomeModel
import kotlin.reflect.KFunction1

data class ApiResult<T>(
    val data: T?,
    val error: NetError?
) {
    companion object {
        fun <T> empty() = ApiResult<T>(null, null)
    }

    inline fun result(onFailure: (NetError) -> Unit, onSuccess: (T) -> Unit) {
        data?.let {
            onSuccess(data)
        }
        error?.let {
            onFailure(error)
        }
    }

    fun isEmpty() = data == null && error == null
}

fun <T> T.success() = ApiResult(this, null)

fun <T> NetError.error() = ApiResult<T>(null, this)