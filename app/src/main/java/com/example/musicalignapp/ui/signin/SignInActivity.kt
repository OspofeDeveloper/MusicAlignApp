package com.example.musicalignapp.ui.signin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ActivityAddFileBinding
import com.example.musicalignapp.databinding.ActivitySigninBinding
import com.example.musicalignapp.ui.align.AlignViewModel

class SignInActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, SignInActivity::class.java)
        }
    }

    private lateinit var binding: ActivitySigninBinding
    private lateinit var signInViewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        signInViewModel = ViewModelProvider(this)[SignInViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.viewBottom.tvFooter.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initUIState() {

    }
}