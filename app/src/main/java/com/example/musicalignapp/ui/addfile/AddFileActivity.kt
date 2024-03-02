package com.example.musicalignapp.ui.addfile

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.ui.align.AlignViewModel
import com.example.musicalignapp.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddFileActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, AddFileActivity::class.java)
        }
    }

    private lateinit var binding: ActivityAddFileBinding
    private lateinit var addFileViewModel: AddFileViewModel

    private var intentGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            addFileViewModel.onImageSelected(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFileViewModel = ViewModelProvider(this)[AddFileViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            addFileViewModel.onNameChanged(text)
        }

        binding.fabImage.setOnClickListener {
            getImageFromGallery()
        }

        binding.btnUploadFile.setOnClickListener {
            addFileViewModel.onAddProductSelected {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun getImageFromGallery() {
        intentGalleryLauncher.launch("image/*")
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.uiState.collect {
                    binding.pbLoading.isVisible = it.isLoading
                    binding.btnUploadFile.isEnabled = it.isValidPackage()
                    showImage(it.imageUrl)

                    if(!it.error.isNullOrBlank()) {
                        //Hacer un dialogo de error
                    }
                }
            }
        }
    }

    private fun showImage(imageUrl: String) {
        val emptyImage = imageUrl.isEmpty()

        binding.viewUploadImage.apply {
            llPlaceHolder.isVisible = emptyImage
            flImage.isVisible = !emptyImage
            Glide.with(this@AddFileActivity).load(imageUrl).into(ivImage)
        }

    }
}