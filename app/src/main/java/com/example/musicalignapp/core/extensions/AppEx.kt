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

fun File.getContent(): String {
    val inputStream = FileInputStream(this)
    val bytes = inputStream.readBytes()
    inputStream.close()

    return String(bytes)
}