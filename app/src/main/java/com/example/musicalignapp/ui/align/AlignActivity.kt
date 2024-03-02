package com.example.musicalignapp.ui.align

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAlignBinding
import com.example.musicalignapp.databinding.DialogSaveWarningSelectorBinding
import com.example.musicalignapp.ui.addfile.AddFileActivity
import com.example.musicalignapp.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlignActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, AlignActivity::class.java)
        }
    }

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
        binding.ivBack.setOnClickListener {
            showSaveWarningDialog()
        }
    }

    private fun showSaveWarningDialog() {
        val dialogBinding = DialogSaveWarningSelectorBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnAccept.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dialogBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun initUIState() {

    }
}