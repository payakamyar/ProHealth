package com.projekt.prohealth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.ActivitySplashBinding
import com.projekt.prohealth.utility.Utilities
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    @Inject lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup(){
        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            login()
        }
        binding.apply {
            retryBtn.setOnClickListener {
                login()
            }
            offlineTv.setOnClickListener {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }
        }
    }

    private fun login(){
        if(Utilities.hasNetworkConnection(this)){
            if(auth.currentUser != null)
                startActivity(Intent(this,MainActivity::class.java))
            else
                startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }else{
            binding.loadingGif.visibility = View.GONE
            binding.noConnectionLayout.visibility = View.VISIBLE
        }
    }
}