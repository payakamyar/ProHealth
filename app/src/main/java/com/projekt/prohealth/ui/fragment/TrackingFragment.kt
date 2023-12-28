package com.projekt.prohealth.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentTrackingBinding
import com.projekt.prohealth.service.TrackingService
import com.projekt.prohealth.utility.Constants
import com.projekt.prohealth.utility.LocationPermission
import com.projekt.prohealth.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@AndroidEntryPoint
class TrackingFragment : Fragment() {


    private val mainViewModel: MainViewModel by viewModels()
    private var missingIndexStart = -1
    private var missingIndexEnd = -1
    private lateinit var binding: FragmentTrackingBinding
    private lateinit var locationManager: LocationManager
    private lateinit var setMarkerLocationListener: LocationListener
    private lateinit var mapView:MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentTrackingBinding.inflate(inflater)
        setup()
        requestLocation()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(missingIndexStart != -1 && TrackingService.isServiceRunning){
            missingIndexEnd = TrackingService.route.value!!.size -1
            for(spot in missingIndexStart .. missingIndexEnd)
                drawPathOnMap(TrackingService.route.value!![spot].latitude,TrackingService.route.value!![spot].longitude)
        }

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
        binding.leftSideButton.setOnClickListener {
            Log.i("clicked?", "handleDataFromService: ")
            requireContext().startService(
                Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
            )
        }

        TrackingService.currentState.observe(viewLifecycleOwner) { state->
            handleDataFromService(state)
            if (state == Constants.ACTION_START_OR_RESUME_SERVICE){
                TrackingService.route.observe(viewLifecycleOwner) {
                    if (it.isNotEmpty()){
                        drawPathOnMap(it.last().latitude, it.last().longitude)
                        Log.i("list", it.last().latitude.toString())
                    }
                }
            }
        }

        TrackingService.time.observe(viewLifecycleOwner){
            binding.timerTextview.text = TrackingService.time.value!!.formattedTimeToString
            binding.caloriesTv.text = TrackingService.caloriesBurned.toString()
            binding.distanceTv.text = String.format("%.2f", TrackingService.distance)
            binding.speedTv.text = String.format("%.2f", TrackingService.averageSpeed)
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

    private fun zoomOutToCoverAll(paths:MutableList<GeoPoint>){
        val boundingBox = BoundingBox.fromGeoPointsSafe(paths)
    }

    private fun drawPathOnMap(latitude: Double, longitude: Double){
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle)
        mapView.overlayManager.add(marker)
    }



    @SuppressLint("MissingPermission")
    private fun handleDataFromService(state:String){
        if (LocationPermission.checkLocationPermissions(requireContext())) {
                when(state){
                    Constants.ACTION_STOP_SERVICE -> showStopActionButtons()
                    Constants.ACTION_START_OR_RESUME_SERVICE -> showStartActionButtons()
                    Constants.ACTION_PAUSE_SERVICE -> showPauseActionButtons()
                }

            binding.leftSideButton.setOnClickListener {
                if(binding.leftSideButton.text == resources.getString(R.string.start)){
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                    )
                } else{
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
                }else{
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

    private fun showStartActionButtons(){
        binding.leftSideButton.text = resources.getString(R.string.stop)
        binding.rightSideButton.text = resources.getString(R.string.pause)
        binding.leftSideButton.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_red)
        binding.rightSideButton.visibility = View.VISIBLE
    }
    private fun showPauseActionButtons(){
        binding.leftSideButton.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_red)
        binding.rightSideButton.visibility = View.VISIBLE
        binding.leftSideButton.text = resources.getString(R.string.stop)
        binding.rightSideButton.text = resources.getString(R.string.resume)
    }
    private fun showStopActionButtons(){
        binding.leftSideButton.text = resources.getString(R.string.start)
        binding.leftSideButton.background = ContextCompat.getDrawable(requireContext(),R.drawable.button_light)
        binding.rightSideButton.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        if(TrackingService.isServiceRunning){
            missingIndexStart = TrackingService.route.value!!.size - 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(setMarkerLocationListener)
        Log.i("Fragment", "onDestroy: ")
    }

}