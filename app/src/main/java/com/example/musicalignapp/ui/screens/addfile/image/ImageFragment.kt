package com.example.musicalignapp.ui.screens.addfile.image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.musicalignapp.R
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.FragmentImageBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.image.viewmodel.ImageViewModel
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private val imageViewModel: ImageViewModel by viewModels()
    private val addFileViewModel: AddFileViewModel by activityViewModels()
    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private var intentGalleryImagesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
//                imageViewModel.onImageSelected(uri)
                showImageToCrop(uri)
                val imageName = getFileNameFromUri(uri)
                initDeleteImageListener(imageName ?: "")
                addFileViewModel.setImageToCrop(uri, imageName ?: "")
            }
        }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onResume() {
        addFileViewModel.imageToCrop.value.apply {
            if(this.second.toString().isNotBlank()) {
                showImageToCrop(this.second)
            }
        }
        super.onResume()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.cvImage.setOnClickListener {
            getImageFromGallery()
        }
    }

    private fun getImageFromGallery() {
        intentGalleryImagesLauncher.launch(arrayOf("image/*"))
    }

    private fun showImageToCrop(uri: Uri) {
        binding.apply {
            llPlaceHolder.isVisible = false
            flImage.isVisible = true
            cvImage.isEnabled = false
            ivImage.setImageURI(uri)
        }
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                imageViewModel.uiState.collect {
                    when (it) {
                        is ScreenState.Empty -> onEmptyState()
                        is ScreenState.Error -> onErrorState(it.error)
                        is ScreenState.Loading -> onLoadingState()
                        is ScreenState.Success -> onSuccessState(it.data)
                    }
                }
            }
        }
    }

    private fun onLoadingState() {
        showImageShimmer()
    }

    private fun onErrorState(error: String) {
        stopImageShimmer()
        requireContext().showToast(error)
    }

    private fun onEmptyState() {
        stopImageShimmer()
        binding.apply {
            llPlaceHolder.isVisible = true
            flImage.isVisible = false
            cvImage.isEnabled = true
        }
    }

    private fun onSuccessState(data: ImageUIModel) {
        stopImageShimmer()
        showImage(data)
    }

    private fun showImage(data: ImageUIModel) {
        if (data.imageUri.isNotBlank()) {
            Glide.with(requireContext()).load(data.imageUri).into(binding.ivImage)
            initDeleteImageListener(data.id)
        }
        binding.apply {
            llPlaceHolder.isVisible = false
            flImage.isVisible = true
            cvImage.isEnabled = false
        }
    }

    private fun initDeleteImageListener(imageId: String) {
        binding.ivDeleteImage.setOnClickListener {
            addFileViewModel.setImageToCrop("".toUri(), "")
            imageViewModel.deleteUploadedImage(imageId, addFileViewModel.getNumImage())
        }
    }

    private fun showImageShimmer() {
        binding.apply {
            cvImage.visibility = View.INVISIBLE
            binding.viewUploadImageShimmer.shimmerImage.visibility = View.VISIBLE
            viewUploadImageShimmer.shimmerImage.startShimmer()
        }
    }

    private fun stopImageShimmer() {
        binding.cvImage.isVisible = true
        binding.viewUploadImageShimmer.shimmerImage.visibility = View.INVISIBLE
        binding.viewUploadImageShimmer.shimmerImage.stopShimmer()
    }
}