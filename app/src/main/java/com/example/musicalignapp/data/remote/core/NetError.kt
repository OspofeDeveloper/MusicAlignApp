package com.example.musicalignapp.data.remote.core

import com.example.musicalignapp.R
import retrofit2.HttpException
import java.io.IOException

sealed class NetError {
    private var stringRes: Int = 0

    data class Server(val code: Int) : NetError()
    object Connectivity : NetError()
    data class Unknown(val message: String) : NetError()

    fun errorResourceMessage(overrideCode: Map<Int, Int>? = null): NetError {
        this.stringRes = when (this) {
            Connectivity -> R.string.error_code_def
            is Server -> {
                overrideCode?.get(code) ?: getDefaultError(code)
            }

            is Unknown -> R.string.error_code_def
        }
        return this
    }

    private fun getDefaultError(code: Int): Int {
        return when (code) {
            300 -> R.string.error_code_300
            301 -> R.string.error_code_301
            302 -> R.string.error_code_302
            303 -> R.string.error_code_303
            304 -> R.string.error_code_304
            305 -> R.string.error_code_305
            306 -> R.string.error_code_306
            307 -> R.string.error_code_307
            308 -> R.string.error_code_308
            400 -> R.string.error_code_400
            401 -> R.string.error_code_401
            402 -> R.string.error_code_402
            403 -> R.string.error_code_403
            404 -> R.string.error_code_404
            405 -> R.string.error_code_405
            406 -> R.string.error_code_406
            407 -> R.string.error_code_407
            408 -> R.string.error_code_408
            409 -> R.string.error_code_409
            410 -> R.string.error_code_410
            411 -> R.string.error_code_411
            412 -> R.string.error_code_412
            413 -> R.string.error_code_413
            414 -> R.string.error_code_414
            415 -> R.string.error_code_415
            416 -> R.string.error_code_416
            417 -> R.string.error_code_417
            418 -> R.string.error_code_418
            421 -> R.string.error_code_421
            422 -> R.string.error_code_422
            423 -> R.string.error_code_423
            424 -> R.string.error_code_424
            425 -> R.string.error_code_425
            426 -> R.string.error_code_426
            428 -> R.string.error_code_428
            429 -> R.string.error_code_429
            431 -> R.string.error_code_431
            451 -> R.string.error_code_451
            500 -> R.string.error_code_500
            501 -> R.string.error_code_501
            502 -> R.string.error_code_502
            503 -> R.string.error_code_503
            504 -> R.string.error_code_504
            505 -> R.string.error_code_505
            506 -> R.string.error_code_506
            507 -> R.string.error_code_507
            508 -> R.string.error_code_508
            510 -> R.string.error_code_510
            511 -> R.string.error_code_511
            else -> R.string.error_code_def
        }
    }

    fun getResourceMessage() = stringRes
}

fun Exception.toError(): NetError = when (this) {
    is IOException -> NetError.Connectivity
    is HttpException -> NetError.Server(code())
    else -> NetError.Unknown(message ?: "")
}