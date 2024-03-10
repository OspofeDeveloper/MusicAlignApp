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
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicalignapp.R
import com.example.musicalignapp.core.Constants.ALIGN_SCREEN_EXTRA_ID
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.databinding.DialogTaskDoneCorrectlyBinding
import com.example.musicalignapp.databinding.DialogWarningSelectorBinding
import com.example.musicalignapp.ui.core.MyJavaScriptInterface
import com.example.musicalignapp.ui.screens.home.HomeActivity
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
                    Log.d("AlignActivity", it.fileContent)
                    Glide.with(this@AlignActivity).load(it.imageUrl).into(binding.ivPartiture)
                    initWebView(it.fileContent, it.listElementIds)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(fileContent: String, listElementIds: List<String>) {
        listElementIds.forEach {
            Log.d("AlignActivity", it)
        }

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
            jsInterface.uiState.collect {
                if(!it.listElementIds.contains("initialList")) {
                    binding.pbLoadingSaving.isVisible = true
                    alignViewModel.saveAlignmentResults(
                        it.listElementIds,
                        intent.getStringExtra(ALIGN_SCREEN_EXTRA_ID)!!
                    ) {
                        binding.pbLoadingSaving.isVisible = false
                        showChangesSavedSuccessfully()
                    }
                }
            }
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
            binding.webView.evaluateJavascript("initSaveResults();", null)
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