package com.example.musicalignapp.ui.screens.addfile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.musicalignapp.R
import com.example.musicalignapp.core.extensions.ifNotEmpty
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.core.enums.FileFragmentType
import com.example.musicalignapp.ui.core.enums.ImageFragmentType
import com.example.musicalignapp.ui.screens.addfile.file.FileFragment
import com.example.musicalignapp.ui.screens.addfile.image.ImageFragment
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import com.example.musicalignapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.android.BuildConfig
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
    private var croppedImage: ImageUIModel? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            myOnBackPressed()
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val cropRect = result.cropRect
            val x = cropRect?.left ?: 0
            val y = cropRect?.top ?: 0

            result.uriContent?.let {
                showSaveCropImageDialog(result.uriContent!!, x, y)
            } ?: run {
                DialogUtils.GenericDialogs.showErrorDialog(
                    getString(R.string.generic_error_message),
                    layoutInflater,
                    this@AddFileActivity
                )
            }
        } else {
            result.error?.localizedMessage?.let {
                DialogUtils.GenericDialogs.showErrorDialog(it, layoutInflater, this@AddFileActivity)
            } ?: run {
                DialogUtils.GenericDialogs.showErrorDialog(
                    getString(R.string.generic_error_message),
                    layoutInflater,
                    this@AddFileActivity
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addFileViewModel = ViewModelProvider(this)[AddFileViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initFragments()
        initListeners()
        initUIState()
    }

    private fun initFragments() {
        initFileFragment()
        initImageFragment()
    }

    private fun initImageFragment() {
        val imageFragment = ImageFragment.create(ImageFragmentType.ADD_FILE)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.imageFragment, imageFragment)
            .commit()
    }

    private fun initFileFragment() {
        val fileFragment = FileFragment.create(FileFragmentType.ADD_FILE)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fileFragment, fileFragment)
            .commit()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener { myOnBackPressed() }
        binding.btnUploadPackage.setOnClickListener {
            val version = this.packageManager.getPackageInfo(this.packageName, 0).versionName
            if(addFileViewModel.packageState.value.hesSameImagesAsFiles()) {
                addFileViewModel.onAddProductSelected(version)
            } else {
                showToast("El numero de imagenes no coincide con el numero de ficheros")
            }
        }
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.uiState.collect {
                    when (it) {
                        is ScreenState.Empty -> {
                            binding.pbLoading.isVisible = false
                        }

                        is ScreenState.Error -> onErrorState(it.error)
                        is ScreenState.Loading -> onLoadingState()
                        is ScreenState.Success -> onSuccessState()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.packageState.collect {
                    binding.btnUploadPackage.isEnabled = it.isValidPackage()
                    binding.tvProjectName?.text =
                        getString(R.string.addfile_project_name, it.projectName)
                }
            }
        }

        addFileViewModel.cropImageSize.observe(this) { size ->
            croppedImage?.let { image ->
                size?.let {
                    val newImage = getNewImage(image, size)
                    addFileViewModel.onCropImageUploaded(newImage)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.imageToCrop.collect { imageToCrop ->
                    if (imageToCrop.second.toString().isNotBlank()) {
                        binding.btnCropImage?.isEnabled = true
                        binding.btnCropImage?.setOnClickListener {
                            cropImage.launch(
                                CropImageContractOptions(
                                    uri = imageToCrop.second,
                                    cropImageOptions = CropImageOptions()
                                )
                            )
                        }
                    } else {
                        binding.btnCropImage?.isEnabled = false
                    }
                }
            }
        }
    }

    private fun onSuccessState() {
        binding.pbLoading.isVisible = false
        setResult(RESULT_OK)
        finish()
    }

    private fun onLoadingState() {
        binding.pbLoading.isVisible = true
    }

    private fun onErrorState(error: String) {
        binding.pbLoading.isVisible = false
        DialogUtils.GenericDialogs.showErrorDialog(error, layoutInflater, this@AddFileActivity)
    }

    private fun showSaveCropImageDialog(uri: Uri, originX: Int, originY: Int) {
        val cropImageName: String = getCropImageName(addFileViewModel.imageToCrop.value.first)

        DialogUtils.AddFileDialogs.showSaveCropImageDialog(
            uri,
            cropImageName,
            layoutInflater,
            this@AddFileActivity
        ) {
            addFileViewModel.saveCropImage(uri, originX, originY, cropImageName, onChangesSaved = { image ->
                croppedImage = image
                addFileViewModel.getImageSize(image.imageUri, false)
                showToast(getString(R.string.safe_done_correctly_title))
            }) {
                DialogUtils.GenericDialogs.showErrorDialog(
                    getString(R.string.generic_error_message),
                    layoutInflater,
                    this@AddFileActivity
                )
            }
        }
    }

    private fun getNewImage(image: ImageUIModel, imageSize: Pair<Int, Int>?): ImageUIModel {
        return imageSize?.let {
            ImageUIModel(
                id = image.id,
                imageUri = image.imageUri,
                width = imageSize.first,
                height = imageSize.second,
                originX = image.originX,
                originY = image.originY
            )
        } ?: run { image }
    }

    private fun getCropImageName(imageName: String): String {
        val lastIndexOfDot = imageName.lastIndexOf('.')
        return if (lastIndexOfDot != -1 && lastIndexOfDot != imageName.length - 1) {
            val name = imageName.substring(0, lastIndexOfDot)
            val extension = imageName.substring(lastIndexOfDot + 1)
            "$name.${addFileViewModel.getNumImage().toTwoDigits()}.$extension"
        } else {
            showToast(getString(R.string.error_image_name))
            ""
        }
    }

    private fun myOnBackPressed() {
        val filesList = addFileViewModel.packageState.value.filesList
        filesList.ifNotEmpty {
            addFileViewModel.deleteUploadedFile(
                it.first().id.substringBeforeLast(".").substringBeforeLast(".")
            )
        }
        addFileViewModel.deleteImage(onFinish = {
            setResult(RESULT_CANCELED)
            finish()
        }) {
            DialogUtils.GenericDialogs.showErrorDialog(
                getString(R.string.generic_error_message),
                layoutInflater,
                this@AddFileActivity
            )
        }
    }

}