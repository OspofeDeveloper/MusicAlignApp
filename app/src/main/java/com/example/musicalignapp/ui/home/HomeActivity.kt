package com.example.musicalignapp.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicalignapp.databinding.ActivityHomeBinding
import com.example.musicalignapp.domain.model.PackageModel
import com.example.musicalignapp.ui.addfile.AddFileActivity
import com.example.musicalignapp.ui.align.AlignActivity
import com.example.musicalignapp.ui.home.adapter.PackagesAdapter
import com.example.musicalignapp.ui.home.adapter.SpacingDecorator
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

    private val addProductLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
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
        initShimmers()
        initList()
        initUIState()
    }

    private fun initShimmers() {
        binding.shimmerAllProducts.startShimmer()
    }

    private fun initList() {
        packagesAdapter = PackagesAdapter { navigateToAlign() }

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(SpacingDecorator(16))
            adapter = packagesAdapter
        }
    }

    private fun initListeners() {
        binding.fabAddFile.setOnClickListener {
            navigateToAddFile()
        }
    }

    private fun navigateToAlign() {
        startActivity(AlignActivity.create(this))
    }

    private fun navigateToAddFile() {
        addProductLauncher.launch(AddFileActivity.create(this))
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.uiState.collect { state ->
                    setShimmers(state.isLoading)
                    renderAllPackages(state.packages)
                }
            }
        }
    }

    private fun setShimmers(isLoading: Boolean) {
        if(!isLoading) {
            binding.shimmerAllProducts.isVisible = false
            binding.shimmerAllProducts.stopShimmer()
        }
    }

    private fun renderAllPackages(packages: List<PackageModel>) {
        packagesAdapter.updateList(packages)
    }
}