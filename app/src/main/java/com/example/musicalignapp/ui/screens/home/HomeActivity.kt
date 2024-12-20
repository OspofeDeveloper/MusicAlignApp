package com.example.musicalignapp.ui.screens.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
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
import androidx.recyclerview.widget.RecyclerView
import com.example.musicalignapp.R
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.ActivityHomeBinding
import com.example.musicalignapp.databinding.DialogWarningSelectorBinding
import com.example.musicalignapp.domain.model.ProjectHomeModel
import com.example.musicalignapp.ui.core.ScreenState
import com.example.musicalignapp.ui.screens.addfile.AddFileActivity
import com.example.musicalignapp.ui.screens.align.AlignActivity
import com.example.musicalignapp.ui.screens.home.adapter.SpacingDecorator
import com.example.musicalignapp.ui.screens.home.adapter.in_progress.FinishedProjectsAdapter
import com.example.musicalignapp.ui.screens.home.adapter.in_progress.PackagesAdapter
import com.example.musicalignapp.ui.screens.home.viewmodel.HomeViewModel
import com.example.musicalignapp.ui.screens.login.LoginActivity
import com.example.musicalignapp.ui.uimodel.HomeUIModel
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
    private lateinit var finishedProjectsAdapter: FinishedProjectsAdapter

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
        val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0)
        val version = pInfo.versionName
        binding.tvAppVersion?.text = "v$version"
        initListeners()
        initRecyclerview()
        initUIState()
    }

    private fun initListeners() {
        binding.fabAddFile.setOnClickListener {
            navigateToAddFile()
        }

        binding.tvLogout?.setOnClickListener {
            homeViewModel.logout {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.uiState.collect { state ->
                    when (state) {
                        is ScreenState.Error -> errorState(state.error)
                        is ScreenState.Loading -> loadingState()
                        is ScreenState.Success -> successState(state.data)
                        is ScreenState.Empty -> {}
                    }
                }
            }
        }
    }

    private fun successState(data: HomeUIModel) {
        binding.pbLoading.isVisible = false
        renderAllPackages(data.packages)
    }

    private fun loadingState() {
        binding.pbLoading.isVisible = true
    }

    private fun errorState(error: String) {
        binding.pbLoading.isVisible = false
        showToast(error)
    }

    private fun renderAllPackages(packages: List<ProjectHomeModel>) {
        packagesAdapter.updateList(packages.filter { !it.isFinished })
        finishedProjectsAdapter.updateList(packages.filter { it.isFinished })
    }

    private fun initRecyclerview() {
        packagesAdapter = PackagesAdapter(
            onItemSelected = { packageName, imageUrl -> navigateToAlign(packageName, imageUrl) },
            onDeletePackageSelected = { projectName ->
                showSaveDeleteWarningDialog(
                    projectName
                )
            }
        )

        finishedProjectsAdapter = FinishedProjectsAdapter(
            onItemSelected = { packageName, imageUrl -> navigateToAlign(packageName, imageUrl) },
            onDeletePackageSelected = { projectName ->
                showSaveDeleteWarningDialog(
                    projectName
                )
            }
        )

        binding.rvInProgress?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            addItemDecoration(SpacingDecorator(16))
            adapter = packagesAdapter
        }

        binding.rvFinished?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            addItemDecoration(SpacingDecorator(16))
            adapter = finishedProjectsAdapter
        }
    }

    private fun showSaveDeleteWarningDialog(
        packageId: String,
    ) {
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
                homeViewModel.deletePackage(
                    packageId
                ) { alertDialog.dismiss() }

            }
            btnCancel.setOnClickListener { alertDialog.dismiss() }
        }

        alertDialog.show()
    }

    private fun navigateToAlign(packageName: String, imageUrl: String) {
        alignScreenLauncher.launch(AlignActivity.create(this, packageName, imageUrl))
        finish()
    }

    private fun navigateToAddFile() {
        addPackageLauncher.launch(AddFileActivity.create(this))
    }
}