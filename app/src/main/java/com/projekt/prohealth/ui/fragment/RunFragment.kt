package com.projekt.prohealth.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentRunBinding
import com.projekt.prohealth.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment() {

    private val mainViewModel:MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(layoutInflater)
        handlePermissions()
        initViews()

        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun handlePermissions(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            it.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value
                val isPermanentlyDenied = shouldShowRequestPermissionRationale(permission)
                if(isGranted)
                    return@forEach
                else if(isPermanentlyDenied){
                    permanentlyDeniedDialog(permission).show()
                }
            }
        }
        if((ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        requestPermissions(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED))
            AlertDialog.Builder(requireContext())
                .setMessage("Please set the location permission to \"Allow all the time\".")
                .setPositiveButton("Ok"){_,_ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }.show()
    }

    private fun requestPermissions(permissions:Array<String>){
            activityResultLauncher.launch(permissions)
    }

    private fun permanentlyDeniedDialog(permission: String):AlertDialog.Builder =
        AlertDialog.Builder(requireContext())
            .setMessage("You have permanently denied a necessary permission(\"$permission\"). Please grant access to continue the operation.")
            .setPositiveButton("Go to Settings") { _, _ ->
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")))
        }
            .setNegativeButton("Cancel",null)

    private fun initViews(){
        binding.floating.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment2)
        }
    }

}