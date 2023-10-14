package com.projekt.prohealth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.ActivityMainBinding
import com.projekt.prohealth.utility.Constants.ACTION_OPEN_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var navController: NavController
    private var currentFragmentId = R.id.runFragment
    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNav.setupWithNavController(navController)
        navigateToTrackingFragment(intent)
        binding.bottomNav.setOnItemSelectedListener {
            Log.i("item", "id: ${it.itemId} ")
            Log.i("before", "id: ${currentFragmentId} ")
            if(it.itemId != currentFragmentId){
                    when(currentFragmentId){
                        R.id.runFragment -> {
                            if(it.itemId == R.id.statisticsFragment)
                                navController.navigate(R.id.action_runFragment_to_statisticsFragment)
                            else if(it.itemId == R.id.settingsFragment)
                                navController.navigate(R.id.action_runFragment_to_settingsFragment)
                            currentFragmentId = it.itemId
                            return@setOnItemSelectedListener true
                        }
                        R.id.statisticsFragment -> {
                            if(it.itemId == R.id.runFragment)
                                navController.navigate(R.id.action_statisticsFragment_to_runFragment)
                            else if(it.itemId == R.id.settingsFragment)
                                navController.navigate(R.id.action_statisticsFragment_to_settingsFragment)
                            currentFragmentId = it.itemId
                            return@setOnItemSelectedListener true
                        }
                        R.id.settingsFragment -> {
                            if(it.itemId == R.id.runFragment)
                                navController.navigate(R.id.action_settingsFragment_to_runFragment)
                            else if(it.itemId == R.id.statisticsFragment)
                                navController.navigate(R.id.action_settingsFragment_to_statisticsFragment)
                            currentFragmentId = it.itemId
                            return@setOnItemSelectedListener true
                        }
                        else -> return@setOnItemSelectedListener false
                    }
                }
            Log.i("after", "id: ${currentFragmentId} ")
            false

        }
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_ltr)
            .setExitAnim(R.anim.slide_out_ltr)
            .setPopEnterAnim(R.anim.slide_in_ltr)
            .setPopExitAnim(R.anim.slide_out_ltr)
            .build()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment(intent:Intent?){
        if(intent?.action == ACTION_OPEN_TRACKING_FRAGMENT){
            navController.navigate(R.id.action_global_trackingFragment)
        }
    }


}
