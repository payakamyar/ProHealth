package com.projekt.prohealth.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

object LocationPermission {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fragment: Fragment

    @SuppressLint("SuspiciousIndentation")
    fun handleLocationPermissions(fragment: Fragment){
        this.fragment = fragment
        activityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            it.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value
                val isPermanentlyDenied = fragment.shouldShowRequestPermissionRationale(permission)
                if(isGranted)
                    return@forEach
                else if(isPermanentlyDenied){
                    permanentlyDeniedDialog(permission).show()
                }
            }
        }
        if((ActivityCompat.checkSelfPermission(fragment.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(fragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (ActivityCompat.checkSelfPermission(fragment.requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED))
            AlertDialog.Builder(fragment.requireContext())
                .setMessage("Please set the location permission to \"Allow all the time\".")
                .setPositiveButton("Ok"){_,_ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }.show()
    }

    fun checkLocationPermissions(context: Context):Boolean{
        if((ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            return false
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED))
            return false
        return true
    }
    private fun requestPermissions(permissions:Array<String>){
        activityResultLauncher.launch(permissions)
    }

    private fun permanentlyDeniedDialog(permission: String): AlertDialog.Builder =
        AlertDialog.Builder(fragment.requireContext())
            .setMessage("You have permanently denied a necessary permission(\"$permission\"). Please grant access to continue the operation.")
            .setPositiveButton("Go to Settings") { _, _ ->
                fragment.requireContext().startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${fragment.requireActivity().packageName}")))
            }
            .setNegativeButton("Cancel",null)
}