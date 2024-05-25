package com.example.musicalignapp.core.extensions

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Int.toTwoDigits(): String {
    return String.format("%02d", this)
}