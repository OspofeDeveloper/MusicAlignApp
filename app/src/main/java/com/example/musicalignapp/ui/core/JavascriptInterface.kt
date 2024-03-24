package com.example.musicalignapp.ui.core

import android.content.Context
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
    private var _alignedElement = MutableStateFlow(AlignedElementId())
    val alignedElement: StateFlow<AlignedElementId> = _alignedElement

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
    fun sendAlignedElementId(alignedElementId: String, nextElementId: String, type: String) {
        _alignedElement.update {
            it.copy(
                alignedElementId = alignedElementId,
                nextElementId = nextElementId,
                type = type
            )
        }
    }

    @JavascriptInterface
    fun sendNextElementId(nextElementId: String, type: String) {
        _alignedElement.update { it.copy(alignedElementId = nextElementId, type = type) }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

data class AlignedElementId(
    val alignedElementId: String = "",
    val nextElementId: String = "",
    val type: String = ""
)
