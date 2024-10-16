package com.example.musicalignapp.utils

object AlignUtils {

    fun getSystemName(packageId: String, systemNumber: String): String {
        return "$packageId.$systemNumber"
    }
}