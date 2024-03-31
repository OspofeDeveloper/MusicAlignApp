package com.example.musicalignapp.ui.screens.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.musicalignapp.databinding.ActivityLoginBinding
import com.example.musicalignapp.ui.screens.home.HomeActivity
import com.example.musicalignapp.ui.screens.signin.SignInActivity
import dagger.hilt.android.AndroidEntryPoint

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
            navigateToHome()
        }
        binding.tvForgotPassword.setOnClickListener {
            navigateToSignIn()
        }
    }

    private fun navigateToHome() {
        startActivity(HomeActivity.create(this))
    }

    private fun navigateToSignIn() {
        startActivity(SignInActivity.create(this))
    }

    private fun initUIState() {

    }
}