package com.example.musicalignapp.ui.screens.addfile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.databinding.DialogErrorLoadingPackageBinding
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import dagger.hilt.android.AndroidEntryPoint
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

    private var intentGalleryImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                addFileViewModel.onImageSelected(uri)
            }
        }

    private var intentGalleryMusicFilesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    binding.viewUploadFile.apply {
                        tvFileName.text = fileName
                        clFileUploaded.isVisible = true
                        llPlaceHolder.isVisible = false
                    }
                    addFileViewModel.onFileSelected(uri, fileName)
                    binding.viewUploadFile.cvFile.isEnabled = false
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
                    binding.viewUploadImage.pbImage.isVisible = it.isImageDeleting

                    setImageShimmer(it.isImageUploading)
                    setFileShimmer(it.isFileUploading)

                    showImage(it.storageImage)

                    it.storageFile.apply {
                        if (fileUri.isNotBlank() && fileName.isNotBlank() && id.isNotBlank()) {
                            initDeleteFileListener(it.storageFile)
                        }
                    }

                    if (!it.error.isNullOrBlank()) {
                        showErrorDialog(it.error)
                    }
                }
            }
        }
    }

    private fun initDeleteFileListener(storageFile: FileUIModel) {
        binding.viewUploadFile.ivDeleteFile.setOnClickListener {
            addFileViewModel.deleteUploadedFile(storageFile.id) {
                binding.viewUploadFile.apply {
                    tvFileName.text = ""
                    clFileUploaded.isVisible = false
                    llPlaceHolder.isVisible = true
                }
                binding.viewUploadFile.cvFile.isEnabled = true
            }
        }
    }

    private fun setFileShimmer(isFileLoading: Boolean) {
        if (isFileLoading) {
            binding.apply {
                viewUploadFile.cvFile.visibility = View.INVISIBLE
                binding.viewUploadFileShimmer.shimmerFile.visibility = View.VISIBLE
                viewUploadFileShimmer.shimmerFile.startShimmer()
            }
        } else {
            binding.viewUploadFile.cvFile.isVisible = true
            binding.viewUploadFileShimmer.shimmerFile.visibility = View.INVISIBLE
            binding.viewUploadFileShimmer.shimmerFile.stopShimmer()
        }
    }

    private fun setImageShimmer(isImageLoading: Boolean) {
        if (isImageLoading) {
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

    private fun showImage(image: ImageModel) {
        val emptyImage = image.imageUri.isEmpty()

        if (!emptyImage) {
            Glide.with(this@AddFileActivity).load(image.imageUri).into(binding.viewUploadImage.ivImage)
            initDeleteImageListener(image.id)
        }

        binding.viewUploadImage.apply {
            llPlaceHolder.isVisible = emptyImage
            flImage.isVisible = !emptyImage
            cvImage.isEnabled = emptyImage
        }
    }

    private fun initDeleteImageListener(imageId: String) {
        binding.viewUploadImage.ivDeleteImage.setOnClickListener {
            addFileViewModel.deleteUploadedImage(imageId)
        }
    }
}