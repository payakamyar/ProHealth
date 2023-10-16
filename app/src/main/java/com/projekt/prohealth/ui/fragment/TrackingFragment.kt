package com.projekt.prohealth.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentTrackingBinding
import com.projekt.prohealth.service.TrackingService
import com.projekt.prohealth.utility.Constants
import com.projekt.prohealth.utility.LocationPermission
import com.projekt.prohealth.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private lateinit var locationManager: LocationManager
    private lateinit var setMarkerLocationListener: LocationListener
    private lateinit var mapView:MapView

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

        mapView = binding.mapview.apply {
            setTileSource(TileSourceFactory.USGS_SAT)
            setMultiTouchControls(true)
            this.controller.setZoom(2.0)
        }
        handleTracking()

        TrackingService.isTracking.observe(viewLifecycleOwner) { isTracking ->
            if (isTracking)
                TrackingService.route.observe(viewLifecycleOwner) {
                    if (it.last().isNotEmpty())
                        drawPathOnMap(it.last().last().latitude, it.last().last().longitude)
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation(){
        if (!LocationPermission.checkLocationPermissions(requireContext())) {
            LocationPermission.handleLocationPermissions(this)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,0f,setMarkerLocationListener)
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



    @SuppressLint("MissingPermission")
    private fun handleTracking(){
        if (LocationPermission.checkLocationPermissions(requireContext())) {
            binding.leftSideButton.setOnClickListener { it ->
                if(binding.leftSideButton.text == resources.getString(R.string.start)){
                    binding.leftSideButton.text = resources.getString(R.string.stop)
                    it.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_red)
                    binding.rightSideButton.visibility = View.VISIBLE
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                    )

                } else{
                    binding.leftSideButton.text = resources.getString(R.string.start)
                    it.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_light)
                    binding.rightSideButton.visibility = View.GONE
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_STOP_SERVICE}
                    )
                }
            }
            binding.rightSideButton.setOnClickListener {
                if(binding.rightSideButton.text == resources.getString(R.string.pause)){
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_PAUSE_SERVICE }
                    )
                    binding.rightSideButton.text = resources.getString(R.string.resume)
                }else{
                    binding.rightSideButton.text = resources.getString(R.string.pause)
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                    )
                }
            }
        }
        else{
            LocationPermission.handleLocationPermissions(this)
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
    }

}