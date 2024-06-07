package com.example.musicalignapp.ui.screens.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.musicalignapp.databinding.ActivitySplashBinding
import com.example.musicalignapp.ui.screens.home.HomeActivity
import com.example.musicalignapp.ui.screens.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when(splashViewModel.checkDestination()) {
            SplashDestination.Home -> navigateToHome()
            SplashDestination.Login -> navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToHome() {
        finish()
        startActivity(HomeActivity.create(this))
    }
}