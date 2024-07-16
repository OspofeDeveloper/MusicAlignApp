package com.example.musicalignapp.ui.screens.replace_system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.musicalignapp.R
import com.example.musicalignapp.core.Constants.SYSTEM_REPLACE_ID
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.ActivityReplaceSystemBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.file.FileFragment
import com.example.musicalignapp.ui.screens.addfile.image.ImageFragment
import com.example.musicalignapp.ui.core.enums.FileFragmentType
import com.example.musicalignapp.ui.core.enums.ImageFragmentType
import com.example.musicalignapp.ui.screens.replace_system.viewmodel.ReplaceSystemViewModel
import com.example.musicalignapp.utils.DialogUtils
import com.example.musicalignapp.utils.DialogUtils.AddFileDialogs.showSaveCropImageDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReplaceSystemActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context, systemId: String): Intent {
            val intent = Intent(context, ReplaceSystemActivity::class.java)
            intent.putExtra(SYSTEM_REPLACE_ID, systemId)
            return intent
        }
    }

    private lateinit var binding: ActivityReplaceSystemBinding
    private lateinit var replaceSysViewModel: ReplaceSystemViewModel

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                showSaveCropImageDialog(result.uriContent!!)
            } ?: run {
                DialogUtils.GenericDialogs.showErrorDialog(getString(R.string.generic_error_message), layoutInflater, this@ReplaceSystemActivity)
            }
        } else {
            result.error?.localizedMessage?.let {
                DialogUtils.GenericDialogs.showErrorDialog(it, layoutInflater, this@ReplaceSystemActivity)
            } ?: run {
                DialogUtils.GenericDialogs.showErrorDialog(getString(R.string.generic_error_message), layoutInflater, this@ReplaceSystemActivity)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplaceSystemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        replaceSysViewModel = ViewModelProvider(this)[ReplaceSystemViewModel::class.java]
        replaceSysViewModel.setSystemName(intent.extras?.getString(SYSTEM_REPLACE_ID) ?: "")
        initUI()
    }

    private fun initUI() {
        binding.tvProjectName?.text = getString(R.string.system_to_replace_name, intent.extras?.getString(SYSTEM_REPLACE_ID))
        initFragments()
        initListeners()
        initUIState()
    }

    private fun initFragments() {
        initFileFragment()
        initImageFragment()
    }

    private fun initImageFragment() {
        val imageFragment = ImageFragment.create(ImageFragmentType.REPLACE_SYSTEM)

        supportFragmentManager.beginTransaction()
            .replace(R.id.imageFragment, imageFragment)
            .commit()
    }

    private fun initFileFragment() {
        val fileFragment = FileFragment.create(FileFragmentType.REPLACE_SYSTEM)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fileFragment, fileFragment)
            .commit()
    }

    private fun initListeners() {
        binding.ivBack?.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnReplaceSystem?.setOnClickListener {
            replaceSysViewModel.onReplaceSystemSelected {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                replaceSysViewModel.replaceSysState.collect {
                    binding.btnReplaceSystem?.isEnabled = it.isValidPackage()
                    binding.pbLoading?.isVisible = it.loading
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                replaceSysViewModel.imageToCrop.collect { imageToCrop ->
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

    private fun showSaveCropImageDialog(uri: Uri) {
        val cropImageName: String = replaceSysViewModel.imageToCrop.value.first

        showSaveCropImageDialog(uri, cropImageName, layoutInflater, this@ReplaceSystemActivity) {
            replaceSysViewModel.onImageSelected(uri, null)
        }
    }
}