package com.example.musicalignapp.ui.screens.addfile.image

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.musicalignapp.R
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.FragmentImageBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.core.enums.ImageFragmentType
import com.example.musicalignapp.ui.screens.addfile.image.viewmodel.ImageViewModel
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
import com.example.musicalignapp.ui.screens.replace_system.viewmodel.ReplaceSystemViewModel
import com.example.musicalignapp.ui.uimodel.ImageUIModel
import com.example.musicalignapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private val imageViewModel: ImageViewModel by viewModels()

    private val addFileViewModel: AddFileViewModel by activityViewModels()
    private val replaceSysViewModel: ReplaceSystemViewModel by activityViewModels()

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageFragmentType: ImageFragmentType
    private var originalImage: ImageUIModel? = null

    companion object {
        fun create(param: ImageFragmentType): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString(Constants.IMAGE_FRAGMENT_TYPE, param.name)
            fragment.arguments = args
            return fragment
        }
    }

    private var intentGalleryImagesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val imageName = getImageNameFromUri(uri)

                when (imageFragmentType) {
                    ImageFragmentType.ADD_FILE -> {
                        initAddFileDeleteImageListener(imageName ?: "")
                        addFileViewModel.saveOriginalImage(uri, imageName ?: "") { image ->
                            originalImage = image
                            showImageToCrop(uri)
                            addFileViewModel.getImageSize(image.imageUri, true)
                        }
                        addFileViewModel.setImageToCrop(uri, imageName ?: "")
                    }

                    ImageFragmentType.REPLACE_SYSTEM -> {
                        val imageSuffix = imageName?.substringAfterLast(".") ?: "jpg"
                        replaceSysViewModel.onImageSelected(uri, imageSuffix)
                        initReplaceSystemDeleteImageListener()
                        showImageToCrop(uri)
                    }
                }
            }
        }


    private fun getImageNameFromUri(uri: Uri): String? {
        var imageName: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                imageName = cursor.getString(nameIndex)
            }
        }
        return imageName
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
        val paramString = arguments?.getString(Constants.IMAGE_FRAGMENT_TYPE)
        imageFragmentType =
            paramString?.let { ImageFragmentType.valueOf(it) } ?: ImageFragmentType.ADD_FILE
        initUI()
    }

    override fun onResume() {
        when (imageFragmentType) {
            ImageFragmentType.ADD_FILE -> {
                addFileViewModel.imageToCrop.value.apply {
                    if (this.second.toString().isNotBlank()) {
                        showImageToCrop(this.second)
                    }
                }
            }

            ImageFragmentType.REPLACE_SYSTEM -> {}
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

        addFileViewModel.originalImageSize.observe(viewLifecycleOwner) { size ->
            originalImage?.let { image ->
                size?.let {
                    val newImage = getNewImage(image, size)
                    addFileViewModel.onOriginalImageUploaded(newImage)
                    initAddFileDeleteImageListener(image.id)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                replaceSysViewModel.imageToCrop.collect {
                    if (it.second.toString().isNotBlank()) {
                        showImageToCrop(it.second)
                    }
                }
            }
        }
    }

    private fun getNewImage(image: ImageUIModel, imageSize: Pair<Int, Int>?): ImageUIModel {
        return imageSize?.let {
            ImageUIModel(
                id = image.id,
                imageUri = image.imageUri,
                height = imageSize.second,
                width = imageSize.first
            )
        } ?: run { image }
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
            initAddFileDeleteImageListener(data.id)
        }
        binding.apply {
            llPlaceHolder.isVisible = false
            flImage.isVisible = true
            cvImage.isEnabled = false
        }
    }

    private fun initAddFileDeleteImageListener(imageId: String) {
        binding.ivDeleteImage.setOnClickListener {
            addFileViewModel.setImageToCrop("".toUri(), "")
            imageViewModel.deleteUploadedImage(imageId, {addFileViewModel.onImageDeleted()}) {
                DialogUtils.GenericDialogs.showErrorDialog(
                    getString(R.string.generic_error_message),
                    layoutInflater,
                    requireActivity()
                )
            }
        }
    }

    private fun initReplaceSystemDeleteImageListener() {
        binding.ivDeleteImage.setOnClickListener {
            replaceSysViewModel.deleteImage()
            onEmptyState()
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