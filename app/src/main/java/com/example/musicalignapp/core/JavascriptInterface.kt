package com.example.musicalignapp.core

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MyJavaScriptInterface @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meiXml: String
) {
    @JavascriptInterface
    fun getMeiXml(): String {
        return meiXml
    }
}
