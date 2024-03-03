package com.example.musicalignapp.ui.addfile

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.databinding.DialogErrorLoadingPackageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class AddFileActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, AddFileActivity::class.java)
        }
    }

    private lateinit var binding: ActivityAddFileBinding
    private lateinit var addFileViewModel: AddFileViewModel

    private var intentGalleryImagesLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            addFileViewModel.onImageSelected(uri)
        }
    }

    private var intentGalleryMusicFilesLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            addFileViewModel.onFileSelected(uri) {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    binding.viewUploadFile.apply {
                        tvFileName.text = fileName
                        clFileUploaded.isVisible = true
                        llPlaceHolder.isVisible = false
                    }
                    addFileViewModel.setFileName(fileName)
                }
            }
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

        binding.viewUploadImage.cvImage.setOnClickListener {
            getImageFromGallery()
        }

        binding.viewUploadFile.cvFile.setOnClickListener {
            getMusicFileFromGallery()
        }

        binding.btnUploadPackage.setOnClickListener {
            addFileViewModel.onAddProductSelected {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun getMusicFileFromGallery() {
        intentGalleryMusicFilesLauncher.launch(arrayOf("application/octet-stream"))
    }

    private fun getImageFromGallery() {
        intentGalleryImagesLauncher.launch("image/*")
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.uiState.collect {
                    binding.btnUploadPackage.isEnabled = it.isValidPackage()
                    binding.pbLoading.isVisible = it.isPackageLoading
                    showImage(it.imageUrl)
                    setImageShimmer(it.isImageLoading)
                    setFileShimmer(it.isFileLoading)

                    if(!it.error.isNullOrBlank()) {
                        showErrorDialog(it.error)
                    }
                }
            }
        }
    }

    private fun setFileShimmer(isFileLoading: Boolean) {
        if(isFileLoading) {
            binding.apply {
                viewUploadFile.cvFile.visibility = View.INVISIBLE
                viewUploadFileShimmer.shimmerFile.startShimmer()
            }
        } else {
            binding.viewUploadFile.cvFile.isVisible = true
            binding.viewUploadFileShimmer.shimmerFile.stopShimmer()
        }
    }

    private fun setImageShimmer(isImageLoading: Boolean) {
        if(isImageLoading) {
            binding.apply {
                viewUploadImage.cvImage.visibility = View.INVISIBLE
                viewUploadImageShimmer.shimmerImage.startShimmer()
            }
        } else {
            binding.viewUploadImage.cvImage.isVisible = true
            binding.viewUploadImageShimmer.shimmerImage.stopShimmer()
        }
    }

    private fun showErrorDialog(error: String) {
        val dialogBinding = DialogErrorLoadingPackageBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.tvError.text = error

        dialogBinding.btnOk.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
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