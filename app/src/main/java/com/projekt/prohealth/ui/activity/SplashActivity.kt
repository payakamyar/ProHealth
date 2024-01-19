package com.projekt.prohealth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.ActivitySplashBinding
import com.projekt.prohealth.utility.Utilities
import dagger.hilt.android.AndroidEntryPoint
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
        binding.apply {
            retryBtn.setOnClickListener {
                login()
            }
            offlineTv.setOnClickListener {

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