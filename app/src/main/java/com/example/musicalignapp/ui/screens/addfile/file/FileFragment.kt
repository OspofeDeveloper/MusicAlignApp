package com.example.musicalignapp.ui.screens.addfile.file

import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicalignapp.core.Constants.FILE_FRAGMENT_TYPE
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.FragmentFileBinding
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.viewmodel.AddFileViewModel
import com.example.musicalignapp.ui.core.enums.FileFragmentType
import com.example.musicalignapp.ui.screens.replace_system.viewmodel.ReplaceSystemViewModel
import com.example.musicalignapp.ui.uimodel.FileUIModel
import com.example.musicalignapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileFragment : Fragment() {

    private val addFileViewModel: AddFileViewModel by activityViewModels()
    private val replaceSysViewModel: ReplaceSystemViewModel by activityViewModels()

    private var _binding: FragmentFileBinding? = null
    private val binding get() = _binding!!

    private val filesList: MutableList<FileUIModel> = mutableListOf()

    private lateinit var fileFragmentType: FileFragmentType

    companion object {
        fun create(param: FileFragmentType): FileFragment {
            val fragment = FileFragment()
            val args = Bundle()
            args.putString(FILE_FRAGMENT_TYPE, param.name)
            fragment.arguments = args
            return fragment
        }
    }

    private var selectMultipleDocumentsLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { listUris ->
            listUris?.let {
                it.forEach { uri ->
                    requireContext().contentResolver.query(uri, null, null, null, null)
                        ?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            val fileName = cursor.getString(nameIndex)
                            val fileSuffix = cursor.getString(nameIndex).substringAfterLast(".")
                            when (fileFragmentType) {
                                FileFragmentType.ADD_FILE -> addFileViewModel.onFileSelected(uri, fileName)
                                FileFragmentType.REPLACE_SYSTEM -> replaceSysViewModel.onFileSelected(uri, fileSuffix, fileName) { systemName ->
                                    DialogUtils.GenericDialogs.showErrorDialog( "El nombre del archivo seeccionado debe coincidir \n con el nombre del sistema ($systemName)", layoutInflater, requireContext())
                                }
                            }
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
        val paramString = arguments?.getString(FILE_FRAGMENT_TYPE)
        fileFragmentType = paramString?.let { FileFragmentType.valueOf(it) } ?: FileFragmentType.ADD_FILE
        initUI()
    }

    private fun initUI() {
        initListeners()
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
                    when(it) {
                        is ScreenState.Empty -> onEmptyState()
                        is ScreenState.Error -> onErrorState(it.error)
                        is ScreenState.Loading -> onLoadingState()
                        is ScreenState.Success -> onSuccessState(it.data)
                    }
                }
            }
        }

        if(fileFragmentType == FileFragmentType.ADD_FILE) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    addFileViewModel.packageState.collect {
                        if(it.imagesList.isNotEmpty()) {
                            binding.cvFile.isEnabled = true
                        } else {
                            binding.cvFile.isEnabled = false
                        }
                    }
                }
            }
        } else {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    replaceSysViewModel.replaceSysState.collect {
                        if(it.fileUri.toString().isNotBlank()) {
                            onReplaceFileSelected(it.fileName)
                        } else {
                            onEmptyState()
                        }
                    }
                }
            }
        }
    }

    private fun onLoadingState() {
        showFileShimmer()
    }

    private fun onErrorState(error: String) {
        stopFileShimmer()
        requireContext().showToast(error)
    }

    private fun onEmptyState() {
        stopFileShimmer()

        binding.apply {
            tvFileName.text = ""
            clFileUploaded.isVisible = false
            llPlaceHolder.isVisible = true
            cvFile.isEnabled = true
        }
    }

    private fun onSuccessState(fileUiModel: FileUIModel) {
        if(fileFragmentType == FileFragmentType.ADD_FILE) {
            onAddFileSuccessState(fileUiModel)
        }
    }

    private fun onAddFileSuccessState(fileUiModel: FileUIModel) {
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
                initAddFileDeleteFileListener(fileUiModel)
            }

            addFileViewModel.onFileUploaded(sortedFilesList)
        }
    }

    private fun onReplaceFileSelected(fileName: String) {
        binding.apply {
            cvFile.isEnabled = false
            tvFileName.text = fileName
            clFileUploaded.isVisible = true
            llPlaceHolder.isVisible = false
            cvFile.isEnabled = false
        }
        initReplaceFileDeleteFileListener()
    }

    private fun initAddFileDeleteFileListener(storageFile: FileUIModel) {
        binding.ivDeleteFile.setOnClickListener {
            addFileViewModel.deleteUploadedFile(storageFile.id.substringBeforeLast(".").substringBeforeLast("."))
            addFileViewModel.onFileDeleted {
                filesList.clear()
            }
        }
    }

    private fun initReplaceFileDeleteFileListener() {
        binding.ivDeleteFile.setOnClickListener {
            replaceSysViewModel.deleteFile()
            onEmptyState()
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