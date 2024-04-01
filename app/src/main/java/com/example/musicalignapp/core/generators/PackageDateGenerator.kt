package com.example.musicalignapp.core.generators

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class PackageDateGenerator @Inject constructor() : Generator<String> {

    @SuppressLint("SimpleDateFormat")
    override fun generate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
    }
}