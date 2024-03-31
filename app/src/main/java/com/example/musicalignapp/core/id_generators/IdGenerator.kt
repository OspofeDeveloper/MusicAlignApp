package com.example.musicalignapp.core.id_generators

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class IdGenerator @Inject constructor() {
    @SuppressLint("SimpleDateFormat")
    fun generatePackageId(): String {
        return SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    }
}