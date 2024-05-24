package com.example.musicalignapp.ui.screens.addfile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.databinding.DialogErrorLoadingPackageBinding
import com.example.musicalignapp.databinding.DialogSaveCropImageBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.file.FileFragment
import com.example.musicalignapp.ui.screens.addfile.image.ImageFragment
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
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

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                navigateToSaveSaveCropImage(result.uriContent!!)
            } ?: run {
                showErrorDialog("Hubo un problema, por favor intentelo de nuevo")
            }
//            val uriContent = result.uriContent
//            val uriFilePath = result.getUriFilePath(this) // optional usage
        } else {
            val exception = result.error
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
        initFragments()
        initListeners()
        initUIState()
    }

    private fun initFragments() {
        initFileFragment()
        initImageFragment()
    }

    private fun initImageFragment() {
        val imageFragment = ImageFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.imageFragment, imageFragment)
            .commit()
    }

    private fun initFileFragment() {
        val fileFragment = FileFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fileFragment, fileFragment)
            .commit()
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            addFileViewModel.onNameChanged(text)
        }

        binding.btnUploadPackage.setOnClickListener {
            addFileViewModel.onAddProductSelected()
        }
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.uiState.collect {
                    when (it) {
                        is ScreenState.Empty -> {}
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
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.imageToCrop.collect { uri ->
                    binding.btnCropImage?.setOnClickListener {
                        cropImage.launch(
                            CropImageContractOptions(uri = uri, cropImageOptions = CropImageOptions())
                        )
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
        showErrorDialog(error)
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

    private fun navigateToSaveSaveCropImage(uri: Uri) {

    }
}