package com.example.musicalignapp.utils

object DateUtils {

    fun displayToMillis(display: String): Long {
        val parts = display.split(":").map { it.toInt() }
        val hours = parts.getOrElse(0) { 0 }
        val minutes = parts.getOrElse(1) { 0 }
        val seconds = parts.getOrElse(2) { 0 }

        return (hours * 3_600_000L) + (minutes * 60_000L) + (seconds * 1_000L)
    }

    fun millisToDisplay(millis: Long): String {
        val hours = (millis / 3_600_000) % 24
        val minutes = (millis / 60_000) % 60
        val seconds = (millis / 1_000) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}