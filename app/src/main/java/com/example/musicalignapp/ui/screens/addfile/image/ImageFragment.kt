package com.example.musicalignapp.ui.screens.addfile.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageViewModel.onImageSelected(uri)
            }
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
        intentGalleryImagesLauncher.launch("image/*")
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
        addFileViewModel.onImageDeleted()
    }

    private fun onSuccessState(data: ImageUIModel) {
        stopImageShimmer()
        showImage(data)
        addFileViewModel.onImageUploaded(data)
    }

    private fun showImage(data: ImageUIModel) {
        if (data.imageUri.isNotBlank()) {
            Glide.with(requireContext()).load(data.imageUri).into(binding.ivImage)
            //convertImageToBitmap(data.imageUri)
            initDeleteImageListener(data.id)
        }

        binding.apply {
            llPlaceHolder.isVisible = false
            flImage.isVisible = true
            cvImage.isEnabled = false
        }
    }

    private fun convertImageToBitmap(imageUri: String) {
        Glide.with(requireContext()).asBitmap().load(imageUri)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val splitImage = Bitmap.createBitmap(resource, 0, 0, 200, 200)
                    binding.ivImage.setImageBitmap(splitImage)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    private fun initDeleteImageListener(imageId: String) {
        binding.ivDeleteImage.setOnClickListener {
            imageViewModel.deleteUploadedImage(imageId)
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