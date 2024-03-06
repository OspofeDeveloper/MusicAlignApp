package com.example.musicalignapp.ui.core

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import javax.inject.Inject

class MyJavaScriptInterface @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meiXml: String,
    private val listElementIds: List<String>,
    private val packageId: String
) {
    private var _uiState = MutableStateFlow(AlignmentJavascriptElements())
    val uiState: StateFlow<AlignmentJavascriptElements> = _uiState

    @JavascriptInterface
    fun getMeiXml(): String {
        return meiXml
    }

    @JavascriptInterface
    fun getListElements(): String {
        val jsonArray = JSONArray(listElementIds)
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun gatPackageId(): String {
        return packageId
    }

    @JavascriptInterface
    fun getListElementsSize(): Int {
        return listElementIds.size
    }

    @JavascriptInterface
    fun saveResults(elementIds: Array<String>) {
        _uiState.update { it.copy(listElementIds = elementIds.toList()) }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

data class AlignmentJavascriptElements(
    val listElementIds: List<String> = listOf("initialList")
)
