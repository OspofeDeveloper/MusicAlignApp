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
    private val file: String,
    private val listElementIds: List<String>,
    private val packageId: String,
    private val fileId: String,
    private val lastElementId: String,
    private val highestElementId: String
) {
    private var _alignedElement = MutableStateFlow(AlignedElementId())
    val alignedElement: StateFlow<AlignedElementId> = _alignedElement

    @JavascriptInterface
    fun getMeiXml(): String {
        return file
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
    fun getFileId(): String {
        return fileId
    }

    @JavascriptInterface
    fun getListElementsSize(): Int {
        return listElementIds.size
    }

    @JavascriptInterface
    fun sendLastElementId(lastElementId: String) {
        _alignedElement.update {
            it.copy(
                lastElementId = lastElementId,
            )
        }
    }

    @JavascriptInterface
    fun sendFinalElementNum(finalElementNum: String) {
        _alignedElement.update {
            it.copy(
                finalElementNum = finalElementNum
            )
        }
    }

    @JavascriptInterface
    fun sendHighestElementId(highestElementId: String) {
        _alignedElement.update {
            it.copy(
                highestElementId = highestElementId
            )
        }
    }

    @JavascriptInterface
    fun getLastElementId(): String {
        return lastElementId
    }

    @JavascriptInterface
    fun getHighestElementId(): String {
        return highestElementId
    }

    @JavascriptInterface
    fun sendEndOfListReached(isEnd: Boolean) {
        _alignedElement.update { it.copy(isEndOfList = isEnd) }
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

    @JavascriptInterface
    fun sendNextFromPlay(elementId: String, type: String) {
        _alignedElement.update { it.copy(alignedElementId = elementId, type = type) }
    }
}

data class AlignedElementId(
    val alignedElementId: String = "",
    val finalElementNum: String = "",
    val nextElementId: String = "",
    val type: String = "",
    val lastElementId: String = "",
    val highestElementId: String = "",
    val isEndOfList: Boolean = false
)
