package com.example.musicalignapp.ui.screens.align.viewmodel

import android.util.Log
import android.view.MotionEvent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.data.local.drawpoint.DrawPointType
import com.example.musicalignapp.di.InterfaceAppModule
import com.example.musicalignapp.domain.model.ProjectModel
import com.example.musicalignapp.domain.usecases.align.GetAlignmentResultsUseCase
import com.example.musicalignapp.domain.usecases.align.GetPathsToShowUseCase
import com.example.musicalignapp.domain.usecases.align.GetShowPathsUseCase
import com.example.musicalignapp.domain.usecases.align.SaveAlignmentResultsUseCase
import com.example.musicalignapp.domain.usecases.align.SavePathsToShowUseCase
import com.example.musicalignapp.domain.usecases.align.SaveShowPathsUseCase
import com.example.musicalignapp.ui.screens.align.enums.AlignSaveType
import com.example.musicalignapp.ui.screens.align.stylus.DrawPoint
import com.example.musicalignapp.ui.screens.align.stylus.StylusState
import com.example.musicalignapp.ui.uimodel.AlignmentJsonUIModel
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
typealias AlignedStroke = Map<String, String>

@HiltViewModel
class AlignViewModel @Inject constructor(
    private val saveAlignmentResultsUseCase: SaveAlignmentResultsUseCase,
    private val getAlignmentDataUseCase: GetAlignmentResultsUseCase,
    private val savePathsToShowUseCase: SavePathsToShowUseCase,
    private val getPathsToShowUseCase: GetPathsToShowUseCase,
    private val saveShowPathsUseCase: SaveShowPathsUseCase,
    private val getShowPathsUseCase: GetShowPathsUseCase,
    @InterfaceAppModule.PackageDateGeneratorAnnotation private val packageDateGenerator: Generator<String>
) : ViewModel() {

    companion object {
        const val DRAW_POINT_TYPE_START = 0f
        const val DRAW_POINT_TYPE_LINE = 1f
    }

    private var _uiState = MutableStateFlow(AlignState())
    val uiState: StateFlow<AlignState> = _uiState

    private var _stylusState = MutableStateFlow(StylusState())
    val stylusState: StateFlow<StylusState> = _stylusState

    private var _currentSystem = MutableStateFlow("01")
    val currentSystem: StateFlow<String> = _currentSystem

    private var _listPaths = MutableStateFlow<List<Path>>(emptyList())
    val listPaths: StateFlow<List<Path>> = _listPaths

    private var _pathsToShow = MutableStateFlow(1)
    val pathsToShow: StateFlow<Int> = _pathsToShow

    private var _showPaths = MutableStateFlow(true)
    val showPaths: StateFlow<Boolean> = _showPaths

    private var currentPath = mutableListOf<DrawPoint>()
    private var initialPaths: ListPaths = mutableListOf()
    private var currentPathCoordinates = mutableListOf<String>()
    private var alignedNow = mutableListOf<String>()

    fun getData(packageId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getAlignmentDataUseCase(packageId)
            }
            result.file?.let {
                if (result.file.isNotBlank()) {
                    _currentSystem.value =
                        if (result.currentSystem == "00") "01" else result.currentSystem
                    result.listElements.forEach {
                        _uiState.value.alignedElements.add(it)
                    }
                    _uiState.update {
                        it.copy(
                            file = result.file,
                            listElementIds = result.listElements.flatMap { map -> map.keys },
                            lastElementId = result.lastElementId,
                            highestElementId = result.highestElementId,
                            imageUrl = result.imageUri,
                            systemNumber = if (result.currentSystem == "00") "01" else result.currentSystem,
                            maxSystemNumber = result.maxSystemNumber
                        )
                    }
                    _listPaths.value = emptyList()
                } else {
                    _uiState.update { it.copy(error = true) }
                    //Handle error with alertDialog
                }
            } ?: run {
                _uiState.update { it.copy(error = true) }
            }
        }
    }

    fun saveAlignmentResults(
        packageId: String,
        originalImageUrl: String,
        lastElementId: String,
        highestElementId: String,
        saveType: AlignSaveType,
        saveChanges: Boolean,
        isFinish: Boolean,
        onChangesSaved: () -> Unit,
    ) {
        //TODO {Adaptar guardado de datos de json dependiendo del system}
        //TODO {Mirar de implementar las flechas para ir pasando de sistema}
        val alignmentJsonUIModel = AlignmentJsonUIModel(
            packageId,
            "$packageId.${_currentSystem.value}",
            lastElementId,
            highestElementId,
            _uiState.value.alignedElements,
//            _uiState.value.alignedElementsStrokes
        )
        var projectModel = ProjectModel(
            projectName = packageId,
            lastModified = packageDateGenerator.generate(),
            originalImageUrl = originalImageUrl,
            maxNumSystems = uiState.value.maxSystemNumber
        )

        when (saveType) {
            AlignSaveType.NEXT_SYS -> {
                _currentSystem.value = (_currentSystem.value.toInt() + 1).toTwoDigits()
                projectModel = projectModel.copy(
                    currentSystem = _currentSystem.value,
                    isFinished = isFinish,
                )
            }

            AlignSaveType.BACK_SYS -> {
                _currentSystem.value = (_currentSystem.value.toInt() + -1).toTwoDigits()
                projectModel = projectModel.copy(
                    currentSystem = currentSystem.value,
                    isFinished = isFinish,
                )
            }

            AlignSaveType.NORMAL -> {
                projectModel = projectModel.copy(
                    currentSystem = _currentSystem.value,
                    isFinished = isFinish,
                )
            }
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                saveAlignmentResultsUseCase(alignmentJsonUIModel.toDomain(), projectModel, saveChanges)
            }
            if (result) {
                if (saveType == AlignSaveType.NORMAL) {
                    alignedNow.clear()
                    onChangesSaved()
                } else {
                    alignedNow.clear()
                    _uiState.value.alignedElements.clear()
//                    _uiState.value.alignedElementsStrokes.clear()
                    requestRendering(
                        StylusState(
                            path = createPath(emptyList<DrawPoint>().toMutableList()),
                            stroke = Stroke(0f),
                        )
                    )
                    onChangesSaved()
                }
            } else {
                Log.d("AlignActivity", "Error in saving data")
            }
        }
    }

    fun savePathsToDraw(pathsToDraw: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                savePathsToShowUseCase(pathsToDraw)
            }
        }
    }

    fun getPathsToDraw() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getPathsToShowUseCase()
            }
            _pathsToShow.value = result
        }
    }

    fun saveShowPaths(isChecked: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveShowPathsUseCase(isChecked)
            }
        }
    }

    fun getShowPaths() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getShowPathsUseCase()
            }
            _showPaths.value = result
        }
    }

    fun getAlignedNowIsEmpty(): Boolean {
        return alignedNow.isEmpty()
    }

    private fun createPath(drawPath: MutableList<DrawPoint>): Path {
        val path = Path()

        for (point in drawPath) {
            if (point.type == DrawPointType.START) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }
        return path
    }

    fun processMotionEvent(
        motionEvent: MotionEvent,
        pathX: Float,
        pathY: Float,
        onFinishDrawing: () -> Unit
    ): Boolean {
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
                    currentPath.clear()
                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else -> return false
            }

            requestRendering(
                StylusState(
                    path = createPath(currentPath),
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
            val listFloats: List<Float> =
                drawCoordinates.trim().split(",").filter { it.isNotBlank() }.map {
                    it.trim().toFloatOrNull()
                        ?: throw IllegalArgumentException("Invalid float value: $it")
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
                    path = createPath(currentPath),
                )
            )
        }
    }

    fun addElementAligned(elementId: String, strokeWidth: Float) {
        val elementCoordinates = currentPathCoordinates.toList().joinToString(",")
        val newElement: AlignedElement = mapOf(elementId to elementCoordinates)
        alignedNow.add(elementId)
        _uiState.value.alignedElements.add(newElement)
        currentPathCoordinates.clear()
    }

    fun drawElementCoordinates(finalElementNum: String, alignedElementId: String, numChildren: Int) {
        val backListPath = mutableListOf<DrawPoint>()
        val drawCoordinatesList = mutableListOf<String?>()
        val listPaths = mutableListOf<Path>()

        Log.d("Pozo", "finalElementNum = $finalElementNum")
        Log.d("Pozo", "alignedElementId = $alignedElementId")
        Log.d("Pozo", "numChildren = $numChildren")

        for (children in 1..numChildren) {
            getPreviousElementCoordinates(alignedElementId, children, drawCoordinatesList).also {
                if(it.isNotBlank()) {
                    drawCoordinatesList.add(it)
                }
            }
            getNextElementCoordinates(finalElementNum, alignedElementId, children, drawCoordinatesList).also {
                if(it.isNotBlank()) {
                    drawCoordinatesList.add(it)
                }
            }
        }

        drawCoordinatesList.add(getCurrentElementCoordinates(alignedElementId))

        drawCoordinatesList.forEach { drawCoordinates ->
            drawCoordinates?.let {
                var listFloats: List<Float> =
                    drawCoordinates.trim().split(",").filter { it.isNotBlank() }.map {
                        it.trim().toFloatOrNull()
                            ?: throw IllegalArgumentException("Invalid float value: $it")
                    }

                for (i in listFloats.indices step 3) {
                    backListPath.add(
                        when (listFloats[i]) {
                            DRAW_POINT_TYPE_START -> {
                                DrawPoint(listFloats[i + 1], listFloats[i + 2], DrawPointType.START)
                            }

                            else -> {
                                DrawPoint(listFloats[i + 1], listFloats[i + 2], DrawPointType.LINE)
                            }
                        }
                    )
                }
                listPaths.add(createPath(backListPath))
                listFloats = emptyList()
                backListPath.clear()
            } ?: run {
                requestRendering(
                    StylusState(
                        path = createPath(mutableListOf()),
                    )
                )
            }
        }

        _listPaths.value = listPaths.toList()
    }

    private fun getCurrentElementCoordinates(alignedElementId: String): String {
        _uiState.value.alignedElements.firstOrNull { it.containsKey(alignedElementId) }?.let {
            return it.values.joinToString(",")
        } ?: run {
            return ""
        }
    }

    private fun getPreviousElementCoordinates(
        alignedElementId: String,
        children: Int,
        drawCoordinatesList: MutableList<String?>
    ): String {
        var previousElement: String
        var previousElementCoordinates: String
        val currentSystem = alignedElementId.substringBeforeLast('_')
        var currentElementNum = alignedElementId.substringAfterLast("_").toInt()

        if(currentElementNum == 0) {
            return ""
        } else {
            currentElementNum -= children
            previousElement = "${currentSystem}_${currentElementNum}"

            while (currentElementNum >= 0) {
                _uiState.value.alignedElements.firstOrNull { it.containsKey(previousElement) }
                    ?.let {
                        previousElementCoordinates = it.values.joinToString(",")
                        if (drawCoordinatesList.contains(previousElementCoordinates)) {
                            currentElementNum -= 1
                            previousElement = "${currentSystem}_${currentElementNum}"
                        } else {
                            return previousElementCoordinates
                        }
                    } ?: run {
                    currentElementNum -= 1
                    previousElement = "${currentSystem}_${currentElementNum}"
                }
            }
            return ""
        }
    }

    private fun getNextElementCoordinates(
        finalElementNum: String,
        alignedElementId: String,
        children: Int,
        drawCoordinatesList: MutableList<String?>
    ): String {
        var nextElement: String
        var nextElementCoordinates: String
        var currentElementNum = alignedElementId.substringAfterLast("_").toInt()
        val currentSystem = alignedElementId.substringBeforeLast('_')

        if(currentElementNum == finalElementNum.toInt()) {
            return ""
        } else {
            currentElementNum += children
            nextElement = "${currentSystem}_${currentElementNum}"

            while (currentElementNum <= finalElementNum.toInt()) {
                _uiState.value.alignedElements.firstOrNull { it.containsKey(nextElement) }?.let {
                    nextElementCoordinates = it.values.joinToString(",")
                    if (drawCoordinatesList.contains(nextElementCoordinates)) {
                        currentElementNum += 1
                        nextElement = "${alignedElementId.substringBeforeLast('_')}_${currentElementNum}"
                    } else {
                        return nextElementCoordinates
                    }
                } ?: run {
                    currentElementNum += 1
                    nextElement = "${currentSystem}_${currentElementNum}"
                }
            }
            return ""
        }
    }

    fun restartElementAlignment(alignedElementId: String, onElementPrepared: () -> Unit) {
        _uiState.value.alignedElements.removeIf { it.containsKey(alignedElementId) }
//        _uiState.value.alignedElementsStrokes.removeIf { it.containsKey("${alignedElementId}_stroke") }
        alignedNow.removeIf { it == alignedElementId }

        requestRendering(
            StylusState(
                path = createPath(mutableListOf()),
            )
        )
        onElementPrepared()
    }

    fun isElementAligned(alignedElementId: String): Boolean {
        val element =
            _uiState.value.alignedElements.firstOrNull { it.containsKey(alignedElementId) }
        return !element.isNullOrEmpty()
    }
}