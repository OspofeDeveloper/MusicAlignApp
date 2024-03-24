package com.example.musicalignapp.ui.screens.align

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.example.musicalignapp.R
import com.example.musicalignapp.core.Constants.ALIGN_SCREEN_EXTRA_ID
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.databinding.DialogTaskDoneCorrectlyBinding
import com.example.musicalignapp.databinding.DialogWarningSelectorBinding
import com.example.musicalignapp.ui.core.MyJavaScriptInterface
import com.example.musicalignapp.ui.screens.home.HomeActivity
import com.example.stylus.ui.StylusState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlignActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context, packageId: String): Intent {
            val intent = Intent(context, AlignActivity::class.java)
            intent.putExtra(ALIGN_SCREEN_EXTRA_ID, packageId)
            return intent
        }
    }

    private lateinit var binding: ActivityAlignBinding
    private lateinit var alignViewModel: AlignViewModel
    private lateinit var jsInterface: MyJavaScriptInterface
    private lateinit var packageId: String

    private val strokeStyle = Stroke(2F)
    private var stylusState: StylusState by mutableStateOf(StylusState())

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showSaveWarningDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alignViewModel = ViewModelProvider(this)[AlignViewModel::class.java]
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        intent.getStringExtra(ALIGN_SCREEN_EXTRA_ID)?.let {
            packageId = it
            alignViewModel.getData(it)
        }
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            showSaveWarningDialog()
        }
    }

    private fun showSaveWarningDialog() {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.save_warning_dialog_title)
            tvDescription.text = getString(R.string.save_warning_dialog_description)
            btnAccept.setOnClickListener {
                startActivity(HomeActivity.create(this@AlignActivity))
                alertDialog.dismiss()
                finish()
            }
            btnCancel.setOnClickListener { alertDialog.dismiss() }
        }

        alertDialog.show()
    }

    private fun initUIState() {
        lifecycleScope.launch {
            alignViewModel.uiState.collect {
                if (it.fileContent.isNotBlank()) {
                    initComposeView(it.imageUrl, it.initDrawCoordinates)
                    initWebView(it.fileContent, it.listElementIds)
                }
            }
        }
    }

    private fun initComposeView(imageUrl: String, drawCoordinates: String) {
        initComposeUIState()

        binding.composeView.setContent {

            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(modifier = Modifier
                    .clipToBounds()
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale; scaleY = scale
                        translationX = offset.x; translationY = offset.y
                    }
                ) {
                    Row {
                        Spacer(modifier = Modifier.weight(0.5f))
                        BoxWithConstraints(modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "partitura",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .transformable(
                                        state = rememberTransformableState { zoomChange, offsetChange, _ ->
                                            scale = (scale * zoomChange).coerceIn(1f, 10f)

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
                                onSuccess = { initDrawings(drawCoordinates) }
                            )
                            DrawArea(
                                modifier = Modifier.fillMaxSize(),
                                imageScale = scale
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
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
        imageScale: Float
    ) {
        Canvas(modifier = modifier
            .clipToBounds()
            .pointerInteropFilter { event ->
                val pointX = event.x / imageScale
                val pointY = event.y / imageScale
                alignViewModel.processMotionEvent(event, pointX, pointY) {
                    binding.webView.evaluateJavascript("initNextAlignment();", null)
                }
            }
        ) {
            with(stylusState) {
                drawPath(
                    path = this.path,
                    color = Color.Magenta,
                    style = strokeStyle
                )
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(fileContent: String, listElementIds: List<String>) {
        jsInterface = MyJavaScriptInterface(this, fileContent, listElementIds, packageId)
        binding.webView.addJavascriptInterface(jsInterface, "Android")

        binding.webView.loadUrl("file:///android_asset/verovio.html")
        binding.webView.settings.javaScriptEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {}

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pbLoadingWebView.isVisible = false
                initButtonListeners()
                initJavascriptListener()
            }

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
                Log.d("pozo", "Aligned Element: $it")
                when(it.type) {
                    "back" -> {
                        alignViewModel.drawElementCoordinates(it.alignedElementId)
                        initBtnRealign(it.alignedElementId)
                    }
                    "nextFromAlignment" -> {
                        binding.btnReAlign?.isVisible = alignViewModel.isElementAligned(it.alignedElementId)
                        alignViewModel.addElementAligned(it.alignedElementId)
                        alignViewModel.drawElementCoordinates(it.nextElementId)
                    }
                    "nextFromButton" -> {
                        binding.btnReAlign?.isVisible = alignViewModel.isElementAligned(it.alignedElementId)
                        alignViewModel.drawElementCoordinates(it.alignedElementId)
                    }
                }
            }
        }
    }

    private fun initBtnRealign(alignedElementId: String) {
        binding.btnReAlign?.isVisible = alignViewModel.isElementAligned(alignedElementId)

        binding.btnReAlign?.setOnClickListener {
            alignViewModel.restartElementAlignment(alignedElementId) {
                binding.btnReAlign?.isVisible = false
            }
            binding.webView.evaluateJavascript("prepareForRealignment()", null)
        }
    }

    private fun initButtonListeners() {
        binding.btnStart.setOnClickListener {
            binding.webView.evaluateJavascript("initStart();", null)
        }

        binding.btnBack.setOnClickListener {
            binding.webView.evaluateJavascript("initBack();", null)
        }

        binding.btnNext.setOnClickListener {
            binding.webView.evaluateJavascript("initNext();", null)
        }

        binding.btnStop.setOnClickListener {
            binding.webView.evaluateJavascript("initStop();", null)
        }

        binding.tvSaveChanges.setOnClickListener {
            binding.pbLoadingSaving.isVisible = true
            alignViewModel.saveAlignmentResults(
                intent.getStringExtra(ALIGN_SCREEN_EXTRA_ID)!!
            ) {
                binding.pbLoadingSaving.isVisible = false
                showChangesSavedSuccessfully()
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
}