package com.example.musicalignapp.ui.screens.addfile.file

import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
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
import com.example.musicalignapp.core.Constants.DELETE_TYPE
import com.example.musicalignapp.core.Constants.UPLOAD_TYPE
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.FragmentFileBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.file.viewmodel.FileViewModel
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileFragment : Fragment() {

    private val fileViewModel: FileViewModel by viewModels()
    private val addFileViewModel: AddFileViewModel by activityViewModels()

    private var _binding: FragmentFileBinding? = null
    private val binding get() = _binding!!

    private val filesList: MutableList<FileUIModel> = mutableListOf()

    private var selectMultipleDocumentsLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { listUris ->
            listUris?.let {
                it.forEach { uri ->
                    requireContext().contentResolver.query(uri, null, null, null, null)
                        ?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            val fileName = cursor.getString(nameIndex)
                            addFileViewModel.onFileSelected(uri, fileName)
                        }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        initUIState()
    }

    private fun initListeners() {
        binding.cvFile.setOnClickListener {
            getMusicFileFromGallery()
        }
    }

    private fun getMusicFileFromGallery() {
        selectMultipleDocumentsLauncher.launch(arrayOf("application/octet-stream"))
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.fileUIState.collect {
                    Log.d("Pozo4", "fileState = $it")
                    when(it) {
                        is ScreenState.Empty -> onEmptyState()
                        is ScreenState.Error -> onErrorState(it.error)
                        is ScreenState.Loading -> onLoadingState()
                        is ScreenState.Success -> onSuccessState(it.data)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addFileViewModel.packageState.collect {
                    if(it.imagesList.isNotEmpty()) {
                        initListeners()
                        binding.cvFile.isEnabled = true
                    } else {
                        binding.cvFile.isEnabled = false
                    }
                }
            }
        }
    }

    private fun onLoadingState() {
        Log.d("Pozo", "File loading state")
        showFileShimmer()
    }

    private fun onErrorState(error: String) {
        Log.d("Pozo", "File error state")
        stopFileShimmer()
        requireContext().showToast(error)
    }

    private fun onEmptyState() {
        Log.d("Pozo", "File empty state")
        stopFileShimmer()

        binding.apply {
            tvFileName.text = ""
            clFileUploaded.isVisible = false
            llPlaceHolder.isVisible = true
            cvFile.isEnabled = true
        }
    }

    private fun onSuccessState(fileUiModel: FileUIModel) {
        Log.d("Pozo2", "File success state before: $fileUiModel")
        Log.d("Pozo2", "Files list before: ${filesList.map { it.fileName }.joinToString("")}")
        if(!filesList.map { it.fileName }.contains(fileUiModel.fileName)) {
            stopFileShimmer()
            filesList.add(fileUiModel)
            val sortedFilesList = filesList.sortedBy { file -> file.fileName.filter { it.isDigit() } }

            binding.apply {
                tvFileName.text = sortedFilesList.joinToString(", ") { it.fileName }
                clFileUploaded.isVisible = true
                llPlaceHolder.isVisible = false
                cvFile.isEnabled = false
            }

            if (fileUiModel.fileUri.isNotBlank() && fileUiModel.fileName.isNotBlank() && fileUiModel.id.isNotBlank()) {
                initDeleteFileListener(fileUiModel)
            }

            addFileViewModel.onFileUploaded(sortedFilesList)
            Log.d("Pozo2", "File success state: $fileUiModel")
            Log.d("Pozo2", "Files list: ${sortedFilesList.map { it.fileName }.joinToString("")}")
        }
    }

    private fun initDeleteFileListener(storageFile: FileUIModel) {
        binding.ivDeleteFile.setOnClickListener {
            addFileViewModel.deleteUploadedFile(storageFile.id.substringBeforeLast(".").substringBeforeLast("."))
            addFileViewModel.onFileDeleted {
                filesList.clear()
                Log.d("Pozo", "Files list: ${filesList.map { it.fileName }.joinToString("")}")
            }
        }
    }

    private fun showFileShimmer() {
        binding.apply {
            cvFile.visibility = View.INVISIBLE
            binding.viewUploadFileShimmer.shimmerFile.visibility = View.VISIBLE
            viewUploadFileShimmer.shimmerFile.startShimmer()
        }
    }

    private fun stopFileShimmer() {
        binding.cvFile.isVisible = true
        binding.viewUploadFileShimmer.shimmerFile.visibility = View.INVISIBLE
        binding.viewUploadFileShimmer.shimmerFile.stopShimmer()
    }
}