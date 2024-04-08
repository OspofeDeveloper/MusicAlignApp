package com.example.musicalignapp.ui.screens.signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicalignapp.databinding.ActivitySigninBinding
import com.example.musicalignapp.ui.screens.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, SignUpActivity::class.java)
        }
    }

    private lateinit var binding: ActivitySigninBinding
    private lateinit var signUpViewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        signUpViewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
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

        binding.btnCreateAccount.setOnClickListener {
            signUpViewModel.register(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            ) {
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        startActivity(HomeActivity.create(this))
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.isLoading.collect {
                    binding.pbLoading.isVisible = it
                }
            }
        }
    }
}