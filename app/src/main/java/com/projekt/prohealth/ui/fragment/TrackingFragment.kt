package com.projekt.prohealth.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentTrackingBinding
import com.projekt.prohealth.service.TrackingService
import com.projekt.prohealth.utility.Constants
import com.projekt.prohealth.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay


@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private lateinit var locationManager: LocationManager
    private lateinit var setMarkerLocationListener: LocationListener
    private lateinit var drawPathLocationListener: LocationListener
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapView:MapView
    private var isDrawingPath = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentTrackingBinding.inflate(inflater)
        setup()
        requestLocation()
        return binding.root
    }

    private fun setup(){
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setMarkerLocationListener = LocationListener {
            val latitude = it.latitude
            val longitude = it.longitude
            setMarkerOnMap(latitude, longitude)
            mapView.controller.setCenter(GeoPoint(latitude,longitude))
        }
        drawPathLocationListener = LocationListener {
            val latitude = it.latitude
            val longitude = it.longitude
            drawPathOnMap(latitude, longitude)
        }

        mapView = binding.mapview.apply {
            setTileSource(TileSourceFactory.USGS_SAT)
            setMultiTouchControls(true)
            this.controller.setZoom(2.0)
        }
        handleTracking()
    }

    private fun requestLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            handlePermissions()
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,0f,setMarkerLocationListener)
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


    private fun setMarkerOnMap(latitude: Double, longitude: Double) {
        if(binding.loading.visibility == View.VISIBLE){
            binding.loading.visibility = View.GONE
            mapView.controller.setZoom(17.0)
        }
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_location)
        if(mapView.overlayManager.size > 0)
            mapView.overlayManager.removeAt(0)

        mapView.overlayManager.add(0,marker)

    }

    private fun drawPathOnMap(latitude: Double, longitude: Double){
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle)
        mapView.overlayManager.add(marker)
    }

    private fun permanentlyDeniedDialog(permission: String): AlertDialog.Builder =
        AlertDialog.Builder(requireContext())
            .setMessage("You have permanently denied a necessary permission(\"$permission\"). Please grant access to continue the operation.")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")))
            }
            .setNegativeButton("Cancel",null)

    private fun handleTracking(){

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            handlePermissions()
            return
        }
        binding.leftSideButton.setOnClickListener { it ->
            if(binding.leftSideButton.text == resources.getString(R.string.start)){
                binding.leftSideButton.text = resources.getString(R.string.stop)
                it.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_red)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,0f,drawPathLocationListener)
                binding.rightSideButton.visibility = View.VISIBLE
                isDrawingPath = true
                requireContext().startService(
                    Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                )

            } else{
                binding.leftSideButton.text = resources.getString(R.string.start)
                it.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_light)
                binding.rightSideButton.visibility = View.GONE
                locationManager.removeUpdates(drawPathLocationListener)
                isDrawingPath = false
            }
        }
        binding.rightSideButton.setOnClickListener {
            if(binding.rightSideButton.text == resources.getString(R.string.pause)){
                locationManager.removeUpdates(drawPathLocationListener)
                isDrawingPath = false
                binding.rightSideButton.text = resources.getString(R.string.resume)
            }else{
                binding.rightSideButton.text = resources.getString(R.string.pause)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,0f,drawPathLocationListener)
                isDrawingPath = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(setMarkerLocationListener)
        if(isDrawingPath)
            locationManager.removeUpdates(drawPathLocationListener)
    }

}