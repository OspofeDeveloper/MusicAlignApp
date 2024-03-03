package com.example.musicalignapp.ui.screens.align

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicalignapp.core.Constants.ALIGN_SCREEN_EXTRA_ID
import com.example.musicalignapp.core.MyJavaScriptInterface
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.databinding.DialogSaveWarningSelectorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlignActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context, id: String): Intent {
            val intent = Intent(context, AlignActivity::class.java)
            intent.putExtra(ALIGN_SCREEN_EXTRA_ID, id)
            return intent
        }
    }

    private lateinit var binding: ActivityAlignBinding
    private lateinit var alignViewModel: AlignViewModel
    private lateinit var jsInterface : MyJavaScriptInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alignViewModel = ViewModelProvider(this)[AlignViewModel::class.java]

        intent.getStringExtra(ALIGN_SCREEN_EXTRA_ID)?.let {
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
        val dialogBinding = DialogSaveWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnAccept.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dialogBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alignViewModel.uiState.collect {
                    if(it.fileContent.isNotBlank()) initWebView(it.fileContent)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(fileContent: String) {
        jsInterface = MyJavaScriptInterface(this, fileContent)
        binding.webView.addJavascriptInterface(jsInterface, "Android")

        binding.webView.loadUrl("file:///android_asset/verovio.html")
        binding.webView.settings.javaScriptEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.pbLoading.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pbLoading.isVisible = false
                initButtonListeners()
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                //Handle Error With Alert Dialog
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
    }
}