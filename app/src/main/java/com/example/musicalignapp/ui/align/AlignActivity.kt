package com.example.musicalignapp.ui.align

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlignBinding
    private lateinit var alignViewModel: AlignViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alignViewModel = ViewModelProvider(this)[AlignViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {

    }

    private fun initUIState() {

    }
}