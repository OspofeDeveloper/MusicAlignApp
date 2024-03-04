package com.example.musicalignapp.ui.screens.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityHomeBinding
import com.example.musicalignapp.databinding.DialogWarningSelectorBinding
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.ui.screens.addfile.AddFileActivity
import com.example.musicalignapp.ui.screens.align.AlignActivity
import com.example.musicalignapp.ui.screens.home.adapter.PackagesAdapter
import com.example.musicalignapp.ui.screens.home.adapter.SpacingDecorator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var packagesAdapter: PackagesAdapter

    private val addPackageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                homeViewModel.getData()
            }
        }

    private val alignScreenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                homeViewModel.getData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initListeners()
        initList()
        initUIState()
    }

    private fun initList() {
        packagesAdapter = PackagesAdapter(
            onItemSelected = { id -> navigateToAlign(id) },
            onDeletePackageSelected = { packageId, fileId, imageId -> showSaveDeleteWarningDialog(packageId, fileId, imageId) }
        )

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(SpacingDecorator(16))
            adapter = packagesAdapter
        }
    }

    private fun showSaveDeleteWarningDialog(packageId: String, fileId: String, imageId: String) {
        val dialogBinding = DialogWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.safe_delete_warning_dialog_title)
            tvDescription.text = getString(R.string.safe_delete_warning_dialog_description)
            btnAccept.setOnClickListener {
                pbLoading.isVisible = true
                homeViewModel.deletePackage(packageId, fileId, imageId) { alertDialog.dismiss() }
            }
            btnCancel.setOnClickListener { alertDialog.dismiss() }
        }

        alertDialog.show()
    }

    private fun initListeners() {
        binding.fabAddFile.setOnClickListener {
            navigateToAddFile()
        }
    }

    private fun navigateToAlign(id: String) {
        alignScreenLauncher.launch(AlignActivity.create(this, id))
        finish()
    }

    private fun navigateToAddFile() {
        addPackageLauncher.launch(AddFileActivity.create(this))
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.uiState.collect { state ->
                    binding.pbLoading.isVisible = state.isLoading
                    renderAllPackages(state.packages)
                }
            }
        }
    }

    private fun renderAllPackages(packages: List<PackageModel>) {
        packagesAdapter.updateList(packages)
    }
}