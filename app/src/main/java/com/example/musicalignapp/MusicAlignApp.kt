package com.example.musicalignapp

import android.app.Application
import com.bugfender.android.BuildConfig
import com.bugfender.sdk.Bugfender
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicAlignApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Bugfender.init(this, "VywB1jrJEuYRmi4PDeUNezr4pvgwz8yo", BuildConfig.DEBUG, true)
    }
}