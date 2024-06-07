package com.example.musicalignapp.ui.screens.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicalignapp.core.extensions.showToast
import com.example.musicalignapp.databinding.ActivityLoginBinding
import com.example.musicalignapp.ui.screens.home.HomeActivity
import com.example.musicalignapp.ui.screens.signin.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            loginViewModel.login(
                user = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                navigateToHome = { navigateToHome() },
                onError = { showToast(it) }
            )
        }

        binding.tvForgotPassword.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun navigateToHome() {
        finish()
        startActivity(HomeActivity.create(this))
    }

    private fun navigateToSignUp() {
        startActivity(SignUpActivity.create(this))
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.isLoading.collect {
                    binding.pbLoading.isVisible = it
                }
            }
        }
    }
}