package com.example.musicalignapp.ui.screens.align

import android.util.Log
import android.view.MotionEvent
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.data.drawpoint.DrawPointType
import com.example.musicalignapp.domain.usecases.align.GetAlignmentDataUseCase
import com.example.musicalignapp.domain.usecases.align.SaveAlignmentResultsUseCase
import com.example.musicalignapp.ui.uimodel.AlignmentJsonUIModel
import com.example.stylus.ui.DrawPoint
import com.example.stylus.ui.StylusState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

typealias ListPath = List<DrawPoint>
typealias ListPaths = MutableList<ListPath>
typealias AlignedElement = Map<String, String>

@HiltViewModel
class AlignViewModel @Inject constructor(
    private val saveAlignmentResultsUseCase: SaveAlignmentResultsUseCase,
    private val getAlignmentDataUseCase: GetAlignmentDataUseCase
) : ViewModel() {

    companion object {
        const val DRAW_POINT_TYPE_START = 0f
        const val DRAW_POINT_TYPE_LINE = 1f
    }

    private var _uiState = MutableStateFlow(AlignUIState())
    val uiState: StateFlow<AlignUIState> = _uiState

    private var _stylusState = MutableStateFlow(StylusState())
    val stylusState: StateFlow<StylusState> = _stylusState

    private var currentPath = mutableListOf<DrawPoint>()
    private var initialPaths: ListPaths = mutableListOf()
    private var currentPathCoordinates = mutableListOf<String>()

    fun getData(packageId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getAlignmentDataUseCase(packageId)
            }
            if (result.fileContent.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        fileContent = result.fileContent,
                        listElementIds = result.listElements.flatMap {map -> map.keys },
                        imageUrl = result.imageUri,
                        initDrawCoordinates = result.listElements.flatMap { map -> map.values }.joinToString(",")
                    )
                }
            } else {
                _uiState.update { it.copy(error = true) }
                //Handle error with alertDialog
            }
        }
    }

    fun saveAlignmentResults(
        packageId: String,
        onChangesSaved: () -> Unit
    ) {
        val alignmentJsonUIModel = AlignmentJsonUIModel(packageId, _uiState.value.alignedElements)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                saveAlignmentResultsUseCase(
                    alignmentJsonUIModel.toDomain(),
                )
            }
            if (result) {
                onChangesSaved()
            } else {
                Log.d("AlignActivity", "Error in saving data")
            }
        }
    }

    private fun createPath(): Path {
        val path = Path()

        for (point in currentPath) {
            if (point.type == DrawPointType.START) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }
        return path
    }

    fun processMotionEvent(motionEvent: MotionEvent, pathX: Float, pathY: Float, onFinishDrawing: () -> Unit): Boolean {
        if (motionEvent.getToolType(motionEvent.actionIndex) == MotionEvent.TOOL_TYPE_STYLUS) {
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentPath.add(
                        DrawPoint(pathX, pathY, DrawPointType.START)
                    )
                    currentPathCoordinates.add("$DRAW_POINT_TYPE_START, $pathX, $pathY")
                }

                MotionEvent.ACTION_MOVE -> {
                    currentPath.add(DrawPoint(pathX, pathY, DrawPointType.LINE))
                    currentPathCoordinates.add("$DRAW_POINT_TYPE_LINE, $pathX, $pathY")
                }

                MotionEvent.ACTION_UP -> {
                    onFinishDrawing()
                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else -> return false
            }

            requestRendering(
                StylusState(
                    path = createPath()
                )
            )
            return true
        }
        return false
    }

    private fun requestRendering(stylusState: StylusState) {
        _stylusState.update {
            return@update stylusState
        }
    }

    fun drawCoordinates(drawCoordinates: String) {
        val initialPath = mutableListOf<DrawPoint>()

        if (drawCoordinates.isNotBlank()) {
            val listFloats: List<Float> = drawCoordinates.trim().split(",").filter { it.isNotBlank() }.map {
                it.trim().toFloatOrNull() ?: throw IllegalArgumentException("Invalid float value: $it")
            }

            for (i in listFloats.indices step 3) {
                initialPath.add(
                    when (listFloats[i]) {
                        DRAW_POINT_TYPE_START -> {
                            if (initialPath.isNotEmpty()) {
                                initialPaths.add(initialPath.toList())
                                initialPath.clear()
                            }
                            DrawPoint(listFloats[i + 1], listFloats[i + 2], DrawPointType.START)
                        }

                        else -> {
                            DrawPoint(listFloats[i + 1], listFloats[i + 2], DrawPointType.LINE)
                        }
                    }
                )
                if (i + 3 == listFloats.size) {
                    initialPaths.add(initialPath)
                }
            }

            initialPaths.forEach { listPath ->
                listPath.forEach { drawPoint ->
                    currentPath.add(drawPoint)
                }
            }

            requestRendering(
                StylusState(
                    path = createPath()
                )
            )
        }
    }

    fun addElementAligned(elementId: String) {
        val elementCoordinates = currentPathCoordinates.joinToString(",")
        val newElement: AlignedElement = mapOf(elementId to elementCoordinates)
        _uiState.value.alignedElements.add(newElement)
        currentPathCoordinates.clear()
    }
}

data class AlignUIState(
    val initDrawCoordinates: String = "",
    val fileContent: String = "",
    val alignedElements: MutableList<AlignedElement> = mutableListOf(),
    val listElementIds: List<String> = emptyList(),
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: Boolean = false
)

inline fun <T> List<T>.findLastIndex(predicate: (T) -> Boolean): Int {
    val iterator = this.listIterator(size)
    var count = 1
    while (iterator.hasPrevious()) {
        val element = iterator.previous()
        if (predicate(element)) {
            return size - count
        }
        count++
    }
    return -1
}