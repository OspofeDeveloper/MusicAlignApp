package com.example.musicalignapp.ui.screens.align

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.example.musicalignapp.R
import com.example.musicalignapp.core.Constants.ALIGN_EXTRA_IMAGE_URL
import com.example.musicalignapp.core.Constants.ALIGN_EXTRA_PACKAGE_ID
import com.example.musicalignapp.core.Constants.CURRENT_ELEMENT_SEPARATOR
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.databinding.DialogAlignInfoBinding
import com.example.musicalignapp.databinding.DialogAlignSettingsBinding
import com.example.musicalignapp.databinding.DialogTaskDoneCorrectlyBinding
import com.example.musicalignapp.databinding.DialogWarningSelectorBinding
import com.example.musicalignapp.ui.core.AlignedElementId
import com.example.musicalignapp.ui.core.MyJavaScriptInterface
import com.example.musicalignapp.ui.core.enums.AlignSaveType
import com.example.musicalignapp.ui.core.enums.PlayModeEnum
import com.example.musicalignapp.ui.screens.align.stylus.StylusState
import com.example.musicalignapp.ui.screens.align.viewmodel.AlignViewModel
import com.example.musicalignapp.ui.screens.home.HomeActivity
import com.example.musicalignapp.ui.screens.replace_system.ReplaceSystemActivity
import com.example.musicalignapp.utils.AlignUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.concurrent.thread

@AndroidEntryPoint
class AlignActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context, packageInfo: String, originalImageUrl: String): Intent {
            val intent = Intent(context, AlignActivity::class.java)
            intent.putExtra(ALIGN_EXTRA_PACKAGE_ID, packageInfo)
            intent.putExtra(ALIGN_EXTRA_IMAGE_URL, originalImageUrl)
            return intent
        }
    }

    private val addPackageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                initUI()
            }
        }

    private lateinit var binding: ActivityAlignBinding
    private lateinit var alignViewModel: AlignViewModel
    private lateinit var jsInterface: MyJavaScriptInterface
    private lateinit var packageId: String
    private var isFinal = false
    private lateinit var dialogSettingsBinding: DialogAlignSettingsBinding

    private var lastElement: String = ""
    private var highestElement: String = ""
    private var isRealignButtonEnabled: Boolean = false
    private var isFirstElement: Boolean = true
    private var alignedElementId: String = ""
    private var alignedElementClass: String = ""
    private var finalElementNum: String = ""
    private var isInitialized: Boolean = false

    private var _strokeStyle = MutableStateFlow(Stroke(3F))
    private val strokeStyle: StateFlow<Stroke> = _strokeStyle

    private var stylusState: StylusState by mutableStateOf(StylusState())

    private var _pathsToDraw = MutableLiveData(0)
    private val pathsToDraw: LiveData<Int> = _pathsToDraw

    private var systemNumber: String = ""
    private var currentFile: String = ""
    private var currentImageUrl: String = ""
    private var numImageLoaded: Int = 0

    private var finalImageHeight: Int = 0
    private var finalImageWidth: Int = 0

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (alignViewModel.getAlignedNowIsEmpty()) {
                startActivity(HomeActivity.create(this@AlignActivity))
                finish()
            } else {
                showExitSaveWarningDialog {
                    startActivity(HomeActivity.create(this@AlignActivity))
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlignBinding.inflate(layoutInflater)
        dialogSettingsBinding = DialogAlignSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alignViewModel = ViewModelProvider(this)[AlignViewModel::class.java]
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        initUI()
    }

    private fun initUI() {
        showImageShimmer()
        intent.getStringExtra(ALIGN_EXTRA_PACKAGE_ID)?.let { packageId ->
            this.packageId = packageId
            alignViewModel.getData(packageId)
        }
        alignViewModel.getShowPaths()
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            if (alignViewModel.getAlignedNowIsEmpty()) {
                startActivity(HomeActivity.create(this@AlignActivity))
                finish()
            } else {
                showExitSaveWarningDialog {
                    startActivity(HomeActivity.create(this@AlignActivity))
                    finish()
                }
            }
        }
    }

    private fun showExitSaveWarningDialog(onAccept: () -> Unit) {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.save_warning_dialog_title)
            tvDescription.text = getString(R.string.save_warning_dialog_description)
            btnAccept.setOnClickListener {
                alertDialog.dismiss()
                onAccept()
            }
            btnCancel.setOnClickListener { alertDialog.dismiss() }
        }

        alertDialog.show()
    }

    private fun initUIState() {
        lifecycleScope.launch {
            alignViewModel.uiState.collect { it ->

                systemNumber = it.systemNumber
                binding.tvTitle.text = getString(R.string.align_title, AlignUtils.getSystemName(packageId, it.systemNumber))
                initComposeSliderView()

                if(it.imageUrl.isNotBlank() && it.imageUrl != currentImageUrl || (it.imageUrl == currentImageUrl && it.file.isNotBlank() && it.file != currentFile)) {
                    numImageLoaded++
                    currentImageUrl = it.imageUrl
                    initComposeView(it.imageUrl, it.initDrawCoordinates)
                }

                if(it.file.isNotBlank() && it.file != currentFile || (it.imageUrl == currentImageUrl && it.imageUrl.isNotBlank() && it.file == currentFile) ) {
                    currentFile = it.file
                    initWebView(
                        it.file,
                        it.systemNumber,
                        it.listElementIds,
                        it.lastElementId,
                        it.highestElementId
                    )
                }

                if ((it.systemNumber == "01" || it.systemNumber == "00")) {
                    binding.ivSystemBack?.visibility = View.INVISIBLE
                } else {
                    binding.ivSystemBack?.visibility = View.VISIBLE
                }

                if(it.systemNumber.isNotBlank()) {
                    if((it.systemNumber == it.maxSystemNumber)) {
                        isFinal = true
                        binding.ivSystemNext?.visibility = View.INVISIBLE
                    } else {
                        isFinal = false
                        binding.ivSystemNext?.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alignViewModel.showPaths.collect {
                    disableCounterButtons(it)
                    dialogSettingsBinding.apply {
                        chkShowPaths.isChecked = it
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alignViewModel.pathsToShow.collect {
                    it.toInt().also { pathsToShow ->
                        dialogSettingsBinding.apply {
                            tvCounter.text = pathsToShow.toString()
                            if(finalElementNum.isNotBlank()) {
                                when (pathsToShow) {
                                    0 -> {
                                        btnDecrementDisabled.isVisible = true
                                        btnDecrementEnabled.isVisible = false
                                    }
                                    finalElementNum.toInt() / 2 -> {
                                        btnIncrementDisabled.isVisible = true
                                        btnIncrementEnabled.isVisible = false
                                    }
                                    else -> {
                                        disableCounterButtons(dialogSettingsBinding.chkShowPaths.isChecked)
                                    }
                                }
                            }

                            if(pathsToShow != -1) {
                                finalElementNum.let {
                                    alignedElementId.let {
                                        if(finalElementNum.isNotBlank() && alignedElementId.isNotBlank()) {
                                            _pathsToDraw.value = pathsToShow
                                            if(!dialogSettingsBinding.chkShowPaths.isChecked) {
                                                drawSurroundElementPaths(null, pathsToShow)
                                                alignViewModel.drawElementCoordinates(finalElementNum, alignedElementId, pathsToShow)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alignViewModel.showPaths.collect {
                    dialogSettingsBinding.chkShowPaths.isChecked = it
                }
            }
        }
    }

    private fun initComposeSliderView() {
        binding.composeViewSlider?.setContent {
            var sliderPosition by remember { mutableStateOf(3f) }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_thin_line),
                    contentDescription = "",
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        _strokeStyle.value = Stroke(it)
                    },
                    colors = SliderDefaults.colors(
                        inactiveTrackColor = Color.LightGray,
                        activeTrackColor = Color.Cyan,
                        thumbColor = Color.Cyan
                    ),
                    valueRange = 0f..6f,
                    modifier = Modifier
                        .height(200.dp)
                        .rotate(0f)
                        .weight(2F)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_thick_line),
                    contentDescription = "",
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }

    private fun initComposeView(imageUrl: String, drawCoordinates: String) {
        initComposeUIState()

        binding.composeView.setContent {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            var componentSize by remember { mutableStateOf(IntSize.Zero) }

            val midpoint: Offset = Offset(
                x = componentSize.width / 2f,
                y = componentSize.height / 2f
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                BoxWithConstraints(modifier = Modifier
                    .clipToBounds()
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .onGloballyPositioned { coordinates ->
                        componentSize = coordinates.size
                    }
                ) {
                    var imageHeight by remember { mutableStateOf(0f) }
                    var imageWidth by remember { mutableStateOf(0f) }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        AsyncImage(
                            model = imageUrl + numImageLoaded.toString(),
                            contentDescription = "partitura",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .transformable(
                                    state = rememberTransformableState { zoomChange, offsetChange, _ ->
                                        scale = (scale * zoomChange).coerceIn(1f, 6f)

                                        val extraWidth = (scale - 1) * constraints.maxWidth
                                        val extraHeight = (scale - 1) * constraints.maxHeight

                                        val maxX = extraWidth / 2
                                        val maxY = extraHeight / 2
                                        offset = Offset(
                                            x = (offset.x + scale * offsetChange.x).coerceIn(
                                                -maxX,
                                                maxX
                                            ),
                                            y = (offset.y + scale * offsetChange.y).coerceIn(
                                                -maxY,
                                                maxY
                                            ),
                                        )
                                    }
                                ),
                            onSuccess = { result ->
                                val originalSize = result.painter.intrinsicSize
                                imageHeight = originalSize.height
                                imageWidth = originalSize.width

                                getOriginalImageSize(imageUrl)
                                initDrawings(drawCoordinates)
                                stopImageShimmer()
                            }
                        )
                        DrawArea(
                            modifier = Modifier.fillMaxSize(),
                            imageScale = scale,
                            componentMidPoint = midpoint,
                            imageHeight = imageHeight,
                            imageWidth = imageWidth
                        )
                    }
                }
            }
        }
    }

    private fun getOriginalImageSize(imageUrl: String) {
        thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val width = bitmap.width
                val height = bitmap.height

                finalImageHeight = height
                finalImageWidth = width

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initDrawings(drawCoordinates: String) {
        alignViewModel.drawCoordinates(drawCoordinates)
    }

    private fun initComposeUIState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alignViewModel.stylusState.collect {
                    stylusState = it
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    fun DrawArea(
        modifier: Modifier = Modifier,
        imageScale: Float,
        componentMidPoint: Offset,
        imageHeight : Float,
        imageWidth: Float
    ) {
        var currentStrokeStyle by remember { mutableStateOf(Stroke()) }
        val listPaths by alignViewModel.listPaths.collectAsState()

        LaunchedEffect(strokeStyle) {
            strokeStyle.collect { newStroke ->
                currentStrokeStyle = newStroke
            }
        }

        Canvas(modifier = modifier
            .clipToBounds()
            .pointerInteropFilter { event ->
                val pointX = event.x / imageScale
                val pointY = event.y / imageScale

                val finalCoordinates = getFinalCoordinates(pointX, pointY, componentMidPoint, imageWidth, imageHeight)

                alignViewModel.processMotionEvent(event, pointX, pointY, finalCoordinates.first, finalCoordinates.second) {
                    binding.webView.evaluateJavascript("initNextAlignment();", null)
                }
            }
        ) {
            with(stylusState) {
                drawPath(
                    path = this.path,
                    color = Color.Blue,
                    style = currentStrokeStyle
                )
            }
            with(listPaths) {
                this.forEachIndexed { index, path ->
                    drawPath(
                        path = path,
                        color = if(index == this.size - 1) Color.Blue else Color.Green,
                        style = currentStrokeStyle
                    )
                }
            }
        }
    }

    private fun getFinalCoordinates(
        pointX: Float,
        pointY: Float,
        componentMidPoint: Offset,
        imageWidth: Float,
        imageHeight: Float
    ): Pair<Int, Int> {
        val relativeX = pointX - componentMidPoint.x
        val relativeY = pointY - componentMidPoint.y

        val imageMidPoint = Offset(imageWidth / 2f, imageHeight / 2f)

        val adjustedX = relativeX + imageMidPoint.x
        val adjustedY = relativeY + imageMidPoint.y

        val scaleFactorX = finalImageWidth / imageWidth
        val scaleFactorY = finalImageHeight / imageHeight

        val originalX = adjustedX * scaleFactorX
        val originalY = adjustedY * scaleFactorY

        return Pair(originalX.toInt(), originalY.toInt())
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(
        file: String,
        systemNumber: String,
        listElementIds: List<String>,
        lastElementId: String,
        highestElementId: String
    ) {
        jsInterface = MyJavaScriptInterface(
            this,
            file,
            listElementIds,
            packageId,
            "$packageId.${if (systemNumber == "00") "01" else systemNumber}",
            lastElementId,
            highestElementId
        )

        binding.webView.apply {
            addJavascriptInterface(jsInterface, "Android")
            loadUrl("file:///android_asset/verovio.html")
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebViewConsole", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId())
                return true
            }
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {}

            override fun onPageFinished(view: WebView?, url: String?) {
                initButtonListeners()
                initJavascriptListener()
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                //Handle Error With Alert Dialog
            }
        }
    }

    private fun initJavascriptListener() {
        lifecycleScope.launch {
            jsInterface.alignedElement.collect {
                lastElement = it.lastElementId
                highestElement = it.highestElementId
                finalElementNum = it.finalElementNum
                alignedElementId = it.alignedElementId

                if (it.lastElementId.endsWith("${CURRENT_ELEMENT_SEPARATOR}0") || it.lastElementId.isBlank()) {
                    isFirstElement = true
                    binding.btnBack.visibility = View.GONE
                    binding.btnBackAligned?.visibility = View.GONE
                    binding.btnBackDisabled?.visibility = View.VISIBLE
                    binding.btnBackAlignedDisabled?.visibility = View.VISIBLE
                } else {
                    isFirstElement = false
                    binding.btnBack.visibility = View.VISIBLE
                    binding.btnBackAligned?.visibility = View.VISIBLE
                    binding.btnBackDisabled?.visibility = View.GONE
                    binding.btnBackAlignedDisabled?.visibility = View.GONE
                }

                if (it.isEndOfList) {
                    binding.btnNext.visibility = View.GONE
                    binding.btnNextAligned?.visibility = View.GONE
                    binding.btnNextDisabled?.visibility = View.VISIBLE
                    binding.btnNextAlignedDisabled?.visibility = View.VISIBLE
                } else {
                    binding.btnNext.visibility = View.VISIBLE
                    binding.btnNextAligned?.visibility = View.VISIBLE
                    binding.btnNextDisabled?.visibility = View.GONE
                    binding.btnNextAlignedDisabled?.visibility = View.GONE
                }

                when (it.type) {
                    "initSystem" -> {
                        val alignedElementSystem = it.alignedElementId.substringAfterLast(".").substringBeforeLast("_")
                        if(alignedElementSystem == systemNumber) {
                            if(!isInitialized) {
                                drawSurroundElementPaths(null, pathsToDraw.value)
                                setBtnRealignedEnable(it)
                            }
                        }
                    }

                    "nextFromAlignment" -> {
                        //TODO: Add element class ("category_id")
                        alignViewModel.addElementAligned(alignedElementId)
                        drawSurroundElementPaths(it.nextElementId, pathsToDraw.value)
                        setBtnRealignedEnable(it)
                    }

                    "nextFromButton" -> {
                        drawSurroundElementPaths(null, pathsToDraw.value)
                        setBtnRealignedEnable(it)
                    }

                    "notAligned" -> {
                        drawSurroundElementPaths(null, pathsToDraw.value)
                        setBtnRealignedEnable(it)
                    }

                    "nextFromPlay" -> {
                        drawSurroundElementPaths(null, pathsToDraw.value)
                    }

                    "back" -> {
                        drawSurroundElementPaths(null, pathsToDraw.value)
                        setBtnRealignedEnable(it)
                    }
                    else -> {
                        setBtnRealignedEnable(it)
                    }
                }

                if(finalElementNum.isNotBlank() && alignedElementId.isNotBlank()) {
                    alignViewModel.getPathsToDraw()
                }
            }
        }
    }

    private fun setBtnRealignedEnable(element: AlignedElementId) {
        val isEnable = alignViewModel.isElementAligned(element.lastElementId)

        if (isEnable) {
            isRealignButtonEnabled = true
            binding.btnReAlign?.isEnabled = true
            binding.btnReAlign?.visibility = View.VISIBLE
            binding.btnReAlignDisabled?.visibility = View.GONE
            initBtnRealign(element.finalElementNum, element.alignedElementId)
        } else {
            isRealignButtonEnabled = false
            binding.btnReAlign?.isEnabled = false
            binding.btnReAlign?.visibility = View.GONE
            binding.btnReAlignDisabled?.visibility = View.VISIBLE
        }
    }

    private fun initBtnRealign(finalElement: String, alignedElementId: String) {
        binding.btnReAlign?.setOnClickListener {
            alignViewModel.restartElementAlignment(alignedElementId) {
                binding.btnReAlign?.isEnabled = false
                binding.btnReAlign?.visibility = View.GONE
                binding.btnReAlignDisabled?.visibility = View.VISIBLE
            }
            alignViewModel.drawElementCoordinates(
                finalElement,
                alignedElementId,
                if(dialogSettingsBinding.chkShowPaths.isChecked) 0 else pathsToDraw.value ?: 0
            )
            binding.webView.evaluateJavascript("prepareForRealignment()", null)
        }
    }

    private fun initButtonListeners() {
        binding.btnStart.setOnClickListener {
            binding.apply {
                btnStart.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                updatePlayModeView(PlayModeEnum.PLAY)
                webView.evaluateJavascript("playAutoMode();", null)
            }
        }

        binding.btnBack.setOnClickListener {
            binding.webView.evaluateJavascript("initBack();", null)
        }

        binding.btnNext.setOnClickListener {
            binding.webView.evaluateJavascript("initNext();", null)
        }

        binding.btnStop.setOnClickListener {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.GONE
            updatePlayModeView(PlayModeEnum.STOP)
            binding.webView.evaluateJavascript("stopAutoMode();", null)
        }

        binding.btnBackAligned?.setOnClickListener {
            binding.webView.evaluateJavascript("initBackNotAligned();", null)
        }

        binding.btnNextAligned?.setOnClickListener {
            binding.webView.evaluateJavascript("initNextNotAligned();", null)
        }

        binding.btnSettings?.setOnClickListener {
            showSettingsDialog()
        }

        binding.tvSaveChanges.setOnClickListener {
            showSaveWarningDialog(isFinal = isFinal) {
                alignViewModel.saveAlignmentResults(
                    intent.getStringExtra(ALIGN_EXTRA_PACKAGE_ID)!!,
                    intent.getStringExtra(ALIGN_EXTRA_IMAGE_URL)!!,
                    lastElement,
                    highestElement,
                    AlignSaveType.NORMAL,
                    true,
                    it
                ) {
                    showChangesSavedSuccessfully()
                }
            }


        }

        binding.ivSystemBack?.setOnClickListener {
            isInitialized = false
            showImageShimmer()
            if(alignViewModel.getAlignedNowIsEmpty()) {
                changeSystem(AlignSaveType.BACK_SYS, false)
            } else {
                showSaveBeforeChangeSystem(AlignSaveType.BACK_SYS)
            }
        }

        binding.ivSystemNext?.setOnClickListener {
            isInitialized = false
            showImageShimmer()
           if(alignViewModel.getAlignedNowIsEmpty()) {
               changeSystem(AlignSaveType.NEXT_SYS, false)
           } else {
               showSaveBeforeChangeSystem(AlignSaveType.NEXT_SYS)
           }
        }
    }

    private fun showSaveBeforeChangeSystem(alignSaveType: AlignSaveType) {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.save_warning_change_system_dialog_title)
            tvDescription.text = getString(R.string.save_change_system_warning_dialog_description)
            btnAccept.text = getString(R.string.guardar_cambios)
            btnCancel.text = getString(R.string.no_guardar)
            btnAccept.setOnClickListener {
                alertDialog.dismiss()
                changeSystem(alignSaveType, true)
            }
            btnCancel.setOnClickListener {
                alertDialog.dismiss()
                changeSystem(alignSaveType, false)
            }
        }

        alertDialog.show()
    }

    private fun changeSystem(alignSaveType: AlignSaveType, saveChanges: Boolean) {
        alignViewModel.saveAlignmentResults(
            intent.getStringExtra(ALIGN_EXTRA_PACKAGE_ID)!!,
            intent.getStringExtra(ALIGN_EXTRA_IMAGE_URL)!!,
            lastElement,
            highestElement,
            alignSaveType,
            saveChanges,
            false
        ) {
            initUI()
        }
    }

    private fun showImageShimmer() {
        binding.apply {
            composeView.visibility = View.INVISIBLE
            webView.visibility = View.INVISIBLE
            imageShimmer?.shimmerImage?.visibility = View.VISIBLE
            imageShimmer?.shimmerImage?.startShimmer()
            webviewShimmer?.shimmerImage?.visibility = View.VISIBLE
            webviewShimmer?.shimmerImage?.startShimmer()
        }
    }

    private fun stopImageShimmer() {
        binding.apply {
            webView.isVisible = true
            composeView.isVisible = true
            webviewShimmer?.shimmerImage?.visibility = View.INVISIBLE
            imageShimmer?.shimmerImage?.visibility = View.INVISIBLE
            webviewShimmer?.shimmerImage?.stopShimmer()
            imageShimmer?.shimmerImage?.stopShimmer()
        }
    }

    private fun updatePlayModeView(type: PlayModeEnum) {
        when (type) {
            PlayModeEnum.PLAY -> {
                binding.apply {
                    btnNextAligned?.visibility = View.GONE
                    btnNext.visibility = View.GONE
                    btnBack.visibility = View.GONE
                    btnBackAligned?.visibility = View.GONE

                    btnNextAlignedDisabled?.visibility = View.VISIBLE
                    btnNextDisabled?.visibility = View.VISIBLE
                    btnBackDisabled?.visibility = View.VISIBLE
                    btnBackAlignedDisabled?.visibility = View.VISIBLE

                    btnReAlign?.visibility = View.GONE
                    btnReAlignDisabled?.visibility = View.VISIBLE
                }
            }

            PlayModeEnum.STOP -> {
                binding.apply {
                    btnNextAligned?.visibility = View.VISIBLE
                    btnNext.visibility = View.VISIBLE
                    btnNextAlignedDisabled?.visibility = View.GONE
                    btnNextDisabled?.visibility = View.GONE

                    if (isRealignButtonEnabled) {
                        btnReAlign?.visibility = View.VISIBLE
                        btnReAlignDisabled?.visibility = View.GONE
                    } else {
                        btnReAlign?.visibility = View.GONE
                        btnReAlignDisabled?.visibility = View.VISIBLE
                    }

                    if (isFirstElement) {
                        btnBack.visibility = View.GONE
                        btnBackAligned?.visibility = View.GONE
                        btnBackDisabled?.visibility = View.VISIBLE
                        btnBackAlignedDisabled?.visibility = View.VISIBLE
                    } else {
                        btnBack.visibility = View.VISIBLE
                        btnBackAligned?.visibility = View.VISIBLE
                        btnBackDisabled?.visibility = View.GONE
                        btnBackAlignedDisabled?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showChangesSavedSuccessfully() {
        val dialogBinding = DialogTaskDoneCorrectlyBinding.inflate(layoutInflater)
        val safeDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        safeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.safe_done_correctly_title)
            tvDescription.text = getString(R.string.safe_done_correctly_description)

            btnAccept.setOnClickListener {
                safeDialog.dismiss()
            }
        }

        safeDialog.show()
    }

    private fun showInfoDialog() {
        val dialogBinding = DialogAlignInfoBinding.inflate(layoutInflater)
        val safeDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        safeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            subdialogAlignButtons.btnAccept.setOnClickListener {
                subdialogAlignButtons.root.visibility = View.GONE
                subdialogAlignColors.root.visibility = View.VISIBLE
            }

            subdialogAlignColors.btnAccept.setOnClickListener {
                showSettingsDialog()
                safeDialog.dismiss()
            }
        }

        safeDialog.show()
    }

    private fun showSettingsDialog() {
        dialogSettingsBinding.root.parent?.let { parent ->
            (parent as ViewGroup).removeView(dialogSettingsBinding.root)
        }

        val safeDialog = AlertDialog.Builder(this).apply {
            setView(dialogSettingsBinding.root)
        }.create()

        safeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogSettingsBinding.apply {

            btnShowInfo.setOnClickListener {
                showInfoDialog()
                safeDialog.dismiss()
            }

            chkShowPaths.setOnCheckedChangeListener { _, isChecked ->
                disableCounterButtons(isChecked)
                if(!isChecked) {
                    alignViewModel.setPathsToDraw(dialogSettingsBinding.tvCounter.text.toString().toInt())
                } else {
                    alignViewModel.drawElementCoordinates("10", alignedElementId, 0)
                }
            }

            btnDecrementEnabled.setOnClickListener {
                alignViewModel.setPathsToDraw(tvCounter.text.toString().toInt() - 1)
            }

            btnIncrementEnabled.setOnClickListener {
                alignViewModel.setPathsToDraw(tvCounter.text.toString().toInt() + 1)
            }

            btnChangeMxl.setOnClickListener {
                val systemName = AlignUtils.getSystemName(packageId, systemNumber)
                safeDialog.dismiss()
                addPackageLauncher.launch(ReplaceSystemActivity.create(this@AlignActivity, systemName))
            }
        }

        safeDialog.setOnDismissListener {
            alignViewModel.saveShowPaths(dialogSettingsBinding.chkShowPaths.isChecked)
            alignViewModel.savePathsToDraw( dialogSettingsBinding.tvCounter.text.toString().toInt())
        }

        safeDialog.show()
    }

    private fun disableCounterButtons(isChecked: Boolean) {
        dialogSettingsBinding.apply {
            btnDecrementDisabled.isVisible = isChecked
            btnIncrementDisabled.isVisible = isChecked
            btnDecrementEnabled.isVisible = !isChecked
            btnIncrementEnabled.isVisible = !isChecked
        }
    }

    private fun drawSurroundElementPaths(nextElementId: String?, pathsToDraw: Int?) {
        if(alignedElementId.isNotBlank()) {
            isInitialized = true
            pathsToDraw?.let {
                nextElementId?.let {
                    alignViewModel.drawElementCoordinates(
                        finalElementNum,
                        it,
                        if(dialogSettingsBinding.chkShowPaths.isChecked) 0 else pathsToDraw
                    )
                } ?: run {
                    alignViewModel.drawElementCoordinates(
                        finalElementNum,
                        alignedElementId,
                        if(dialogSettingsBinding.chkShowPaths.isChecked) 0 else pathsToDraw
                    )
                }
            }
        }
    }

    private fun showSaveWarningDialog(isFinal: Boolean, onAccept: (Boolean) -> Unit) {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.confirma_guardado)
            tvDescription.text = getString(R.string.safe_save_description)

            if(isFinal) {
                btnAccept.setOnClickListener {
                    alertDialog.dismiss()
                    showSaveAndFinishDialog {
                        onAccept(it)
                    }
                }
            } else {
                btnAccept.setOnClickListener {
                    alertDialog.dismiss()
                    onAccept(false)
                }
            }

            btnCancel.setOnClickListener { alertDialog.dismiss() }
        }

        alertDialog.show()
    }

    private fun showSaveAndFinishDialog(onAccept: (Boolean) -> Unit) {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.guardar_y_finalizar)
            tvDescription.text = getString(R.string.save_and_finish_description)
            btnAccept.text = getString(R.string.save_and_finish)
            btnCancel.text = getString(R.string.just_save)

            btnAccept.setOnClickListener {
                alertDialog.dismiss()
                onAccept(true)
            }
            btnCancel.setOnClickListener {
                alertDialog.dismiss()
                onAccept(false)
            }
        }

        alertDialog.show()
    }
}