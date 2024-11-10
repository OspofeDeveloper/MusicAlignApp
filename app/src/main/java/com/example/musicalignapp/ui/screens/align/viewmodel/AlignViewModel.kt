package com.example.musicalignapp.ui.screens.align.viewmodel

import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicalignapp.core.Constants.CURRENT_ELEMENT_SEPARATOR
import com.example.musicalignapp.core.extensions.getBoundingBox
import com.example.musicalignapp.core.extensions.getBoundingBoxArea
import com.example.musicalignapp.core.extensions.toFinalId
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
import com.example.musicalignapp.ui.core.enums.AlignSaveType
import com.example.musicalignapp.ui.screens.align.stylus.DrawPoint
import com.example.musicalignapp.ui.screens.align.stylus.StylusState
import com.example.musicalignapp.ui.uimodel.AlignmentJsonUIModel
import com.example.musicalignapp.ui.uimodel.finaloutput.AnnotationOutput
import com.example.musicalignapp.utils.DateUtils.displayToMillis
import com.example.musicalignapp.utils.DateUtils.millisToDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

typealias ListPath = List<DrawPoint>
typealias ListPaths = MutableList<ListPath>
typealias AlignedElement = Map<String, String>

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

    private var _pathsToShow = MutableStateFlow("-1")
    val pathsToShow: StateFlow<String> = _pathsToShow

    private var _showPaths = MutableStateFlow(true)
    val showPaths: StateFlow<Boolean> = _showPaths

    private var currentPath = mutableListOf<DrawPoint>()
    private var initialPaths: ListPaths = mutableListOf()

    private var currentPathCoordinates = mutableListOf<String>()

    private var currentPolygon = mutableListOf<Int>()
    private var listAnnotations = mutableListOf<AnnotationOutput>()

    private var alignedNow = mutableListOf<String>()
    private var currentAnnotation = AnnotationOutput("", 0, 0, emptyList(), 0, emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    private var startTime: Instant = Instant.now()

    fun getData(packageId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getAlignmentDataUseCase(packageId)
            }
            result.file?.let {
                if (result.file.isNotBlank()) {
                    _currentSystem.value = if (result.currentSystem == "00") "01" else result.currentSystem

                    val listAlignedElements: MutableList<AlignedElement> = mutableListOf()
                    result.listElements.forEach {
                        listAlignedElements.add(it)
                    }
                    listAnnotations = result.finalOutputJsonModel.annotations.toMutableList()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startTime = Instant.now()
                    }

                    _uiState.update {
                        it.copy(
                            file = result.file,
                            listElementIds = result.listElements.flatMap { map -> map.keys },
                            lastElementId = result.lastElementId,
                            highestElementId = result.highestElementId,
                            imageUrl = result.imageUri,
                            systemNumber = if (result.currentSystem == "00") "01" else result.currentSystem,
                            maxSystemNumber = result.maxSystemNumber,
                            alignedElements = listAlignedElements,
                            currentImageId = result.currentImageId,
                            finalOutputJsonModel = result.finalOutputJsonModel,
                        )
                    }
                    _listPaths.value = emptyList()
                } else {
                    _uiState.update { it.copy(error = true) }
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
        isFinishedGood: Boolean,
        onChangesSaved: () -> Unit,
        onChangesNotSaved: () -> Unit,
    ) {
        val alignmentJsonUIModel = AlignmentJsonUIModel(
            packageId,
            "$packageId.${_currentSystem.value}",
            lastElementId,
            highestElementId,
            _uiState.value.alignedElements,
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val duration = Duration.between(startTime, Instant.now())
            val durationMillis = duration.toMillis()
            val projectTimeInMillis = displayToMillis(_uiState.value.finalOutputJsonModel.projectDuration ?: "00:00:00")

            val projectTimeUpdated =  projectTimeInMillis+ durationMillis

            val displayTime = millisToDisplay(projectTimeUpdated)

            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    saveAlignmentResultsUseCase(
                        alignmentJsonUIModel.toDomain(),
                        projectModel,
                        saveChanges,
                        _uiState.value.finalOutputJsonModel.copy(
                            annotations = listAnnotations,
                            isFinished = isFinish,
                            isFinishedGood = isFinishedGood,
                            projectDuration = displayTime
                        )
                    )
                }

                if (result) {
                    if (saveType == AlignSaveType.NORMAL) {
                        alignedNow.clear()
                        onChangesSaved()
                    } else {
                        alignedNow.clear()
                        _uiState.value.alignedElements.clear()
                        requestRendering(
                            StylusState(
                                path = createPath(emptyList<DrawPoint>().toMutableList()),
                                stroke = Stroke(0f),
                            )
                        )
                        onChangesSaved()
                    }
                } else {
                    onChangesNotSaved()
                }
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
            _pathsToShow.value = result.toString()
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
        convertedPathX: Int,
        convertedPathY: Int,
        onFinishDrawing: () -> Unit
    ): Boolean {
        if (motionEvent.getToolType(motionEvent.actionIndex) == MotionEvent.TOOL_TYPE_STYLUS) {
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentPath.add(
                        DrawPoint(pathX, pathY, DrawPointType.START)
                    )
                    currentPathCoordinates.add("$DRAW_POINT_TYPE_START, $pathX, $pathY")
                    currentPolygon.add(convertedPathX)
                    currentPolygon.add(convertedPathY)
                }

                MotionEvent.ACTION_MOVE -> {
                    currentPath.add(DrawPoint(pathX, pathY, DrawPointType.LINE))
                    currentPathCoordinates.add("$DRAW_POINT_TYPE_LINE, $pathX, $pathY")
                    currentPolygon.add(convertedPathX)
                    currentPolygon.add(convertedPathY)
                }

                MotionEvent.ACTION_UP -> {
                    onFinishDrawing()
                    addAnnotation()
                    currentPath.clear()
                }

                MotionEvent.ACTION_CANCEL -> {}

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

    private fun addAnnotation() {
        val safeCurrentPolygon = currentPolygon.toList()
        val bBox = safeCurrentPolygon.getBoundingBox()
        currentAnnotation = AnnotationOutput(
            id = "",
            imageId = _uiState.value.currentImageId,
            categoryId = 0,
            segmentation = safeCurrentPolygon,
            area = bBox.getBoundingBoxArea(),
            bbox = bBox,
        )
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
                    path = createPath(currentPath),
                )
            )
        }
    }

    fun addElementAligned(elementFixId: String, categoryId: Int, elementId: String) {
        if(!listAnnotations.map { it.id }.contains(elementId.toFinalId(_uiState.value.currentImageId))) {
            val elementCoordinates = currentPathCoordinates.toList().joinToString(",")
            val newElement: AlignedElement = mapOf(elementFixId to elementCoordinates)
            alignedNow.add(elementFixId)
            _uiState.value.alignedElements.add(newElement)

            val newAnnotation = currentAnnotation.copy(id = elementId.toFinalId(_uiState.value.currentImageId), categoryId = categoryId)
            listAnnotations.add(newAnnotation)
        }

        currentPolygon.clear()
        currentPathCoordinates.clear()
    }

    fun drawElementCoordinates(
        finalElementNum: String,
        alignedElementId: String,
        numChildren: Int
    ) {
        if(alignedElementId.isNotBlank()) {
            val backListPath = mutableListOf<DrawPoint>()
            val drawCoordinatesList = mutableListOf<String?>()
            val listPaths = mutableListOf<Path>()

            for (children in 1..numChildren) {
                getPreviousElementCoordinates(alignedElementId, children, drawCoordinatesList).also {
                    if (it.isNotBlank()) {
                        drawCoordinatesList.add(it)
                    }
                }
                getNextElementCoordinates(
                    finalElementNum,
                    alignedElementId,
                    children,
                    drawCoordinatesList
                ).also {
                    if (it.isNotBlank()) {
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
        if(alignedElementId.isBlank()) {
           return ""
        }

        var previousElement: String
        var previousElementCoordinates: String
        val currentSystem = alignedElementId.substringBeforeLast(CURRENT_ELEMENT_SEPARATOR)
        var currentElementNum = alignedElementId.substringAfterLast(CURRENT_ELEMENT_SEPARATOR).toInt()

        if (currentElementNum == 0) {
            return ""
        } else {
            currentElementNum -= children
            previousElement = "${currentSystem}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"

            while (currentElementNum >= 0) {
                _uiState.value.alignedElements.firstOrNull { it.containsKey(previousElement) }
                    ?.let {
                        previousElementCoordinates = it.values.joinToString(",")
                        if (drawCoordinatesList.contains(previousElementCoordinates)) {
                            currentElementNum -= 1
                            previousElement =
                                "${currentSystem}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"
                        } else {
                            return previousElementCoordinates
                        }
                    } ?: run {
                    currentElementNum -= 1
                    previousElement =
                        "${currentSystem}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"
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
        if(alignedElementId.isBlank()) {
            return ""
        }
        var nextElement: String
        var nextElementCoordinates: String
        var currentElementNum =
            alignedElementId.substringAfterLast(CURRENT_ELEMENT_SEPARATOR).toInt()
        val currentSystem = alignedElementId.substringBeforeLast(CURRENT_ELEMENT_SEPARATOR)

        if (currentElementNum == finalElementNum.toInt()) {
            return ""
        } else {
            currentElementNum += children
            nextElement = "${currentSystem}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"

            while (currentElementNum <= finalElementNum.toInt()) {
                _uiState.value.alignedElements.firstOrNull { it.containsKey(nextElement) }?.let {
                    nextElementCoordinates = it.values.joinToString(",")
                    if (drawCoordinatesList.contains(nextElementCoordinates)) {
                        currentElementNum += 1
                        nextElement =
                            "${alignedElementId.substringBeforeLast(CURRENT_ELEMENT_SEPARATOR)}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"
                    } else {
                        return nextElementCoordinates
                    }
                } ?: run {
                    currentElementNum += 1
                    nextElement = "${currentSystem}${CURRENT_ELEMENT_SEPARATOR}${currentElementNum}"
                }
            }
            return ""
        }
    }

    fun restartElementAlignment(alignedElementFixId: String, annotationId: String, onElementPrepared: () -> Unit) {
        _uiState.value.alignedElements.removeIf { it.containsKey(alignedElementFixId) }
        listAnnotations.removeIf { it.id == annotationId.toFinalId(_uiState.value.currentImageId) }
        alignedNow.removeIf { it == alignedElementFixId }

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

    fun setPathsToDraw(pathsToDraw: Int) {
        pathsToDraw.toString().also { pathsToString: String ->
            _pathsToShow.update { if (_pathsToShow.value == pathsToString) pathsToDraw.toTwoDigits() else pathsToString }
        }
    }
}