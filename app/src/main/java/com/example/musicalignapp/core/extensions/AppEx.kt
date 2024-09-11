package com.example.musicalignapp.core.extensions

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.FileInputStream

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Int.toTwoDigits(): String {
    return String.format("%02d", this)
}

fun <T> List<T>.ifNotEmpty(action: (List<T>) -> Unit) {
    if (this.isNotEmpty()) {
        action(this)
    }
}

fun String.toFinalId(imageNum: Int): String {
    return "line${imageNum}:${this}"
}