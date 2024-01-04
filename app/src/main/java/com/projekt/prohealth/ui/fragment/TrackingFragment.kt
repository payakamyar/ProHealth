package com.projekt.prohealth.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentTrackingBinding
import com.projekt.prohealth.entity.Run
import com.projekt.prohealth.service.TrackingService
import com.projekt.prohealth.utility.Constants
import com.projekt.prohealth.utility.Utilities
import com.projekt.prohealth.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.sql.Time
import java.time.LocalTime


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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
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
            if(binding.leftSideButton.text == getString(R.string.start))
                requireContext().startService(
                Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                )
        }

        binding.continueRun.setOnClickListener {
            requireContext().startService(
                Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
            )
            requestLocation()
            it.visibility = View.GONE
        }

        TrackingService.currentState.observe(viewLifecycleOwner) { state->
            handleServiceStates(state)
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
        if (!Utilities.checkLocationPermissions(requireContext())) {
            Utilities.handleLocationPermissions(this)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,0f,setMarkerLocationListener)
    }


    private fun setMarkerOnMap(latitude: Double, longitude: Double) {
        if(binding.loading.visibility == View.VISIBLE){
            binding.loading.visibility = View.GONE
            binding.leftSideButton.isEnabled = true
            mapView.controller.setZoom(17.0)
        }
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_location)
        if(mapView.overlayManager.size > 0)
            mapView.overlayManager.removeAt(0)
        mapView.overlayManager.add(0,marker)
    }

    private fun takeSnapShotFromMap():Bitmap{
        val bitmap = Bitmap.createBitmap(mapView.width, mapView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        mapView.draw(canvas)
        return bitmap
    }

    private fun zoomOutToCoverAll(){
        val boundingBox = BoundingBox.fromGeoPointsSafe(TrackingService.route.value)
        val center = boundingBox.centerWithDateLine
        mapView.zoomToBoundingBox(boundingBox,true,200)
        mapView.controller.setCenter(center)
        locationManager.removeUpdates(setMarkerLocationListener)
        try{
        mapView.overlayManager.removeAt(0)
        }catch (e:Exception){
            Log.e("overlay", "zoomOutToCoverAll: NOT FOUND!", ) }
    }

    private fun drawPathOnMap(latitude: Double, longitude: Double){
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle)
        mapView.overlayManager.add(marker)
    }



    @SuppressLint("MissingPermission")
    private fun handleServiceStates(state:String){
        if (Utilities.checkLocationPermissions(requireContext())) {
                when(state){
                    Constants.ACTION_DEEP_PAUSE_SERVICE -> showDeepPauseActionButtons()
                    Constants.ACTION_START_OR_RESUME_SERVICE -> showStartActionButtons()
                    Constants.ACTION_PAUSE_SERVICE -> showPauseActionButtons()
                }

            binding.leftSideButton.setOnClickListener {
                if(binding.leftSideButton.text == resources.getString(R.string.start) || binding.leftSideButton.text == resources.getString(R.string.resume)){
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_START_OR_RESUME_SERVICE }
                    )
                } else if(binding.leftSideButton.text == resources.getString(R.string.pause)){
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_PAUSE_SERVICE }
                    )
                } else{
                    mainViewModel.insertRun(Run(takeSnapShotFromMap(),System.currentTimeMillis(),TrackingService.averageSpeed.toFloat()
                        ,TrackingService.distance, TrackingService.time.value!!.second.toLong(),TrackingService.caloriesBurned))
                    findNavController().popBackStack()
                }
            }
            binding.rightSideButton.setOnClickListener {
                if(binding.rightSideButton.text == resources.getString(R.string.stop)){
                    requireContext().startService(
                        Intent(requireContext(),TrackingService::class.java).also { current-> current.action = Constants.ACTION_DEEP_PAUSE_SERVICE}
                    )
                } else{
                    //discard here
                }
            }
        }
        else{
            Utilities.handleLocationPermissions(this)
        }

    }

    private fun showStartActionButtons(){
        binding.apply {
            leftSideButton.text = resources.getString(R.string.pause)
            rightSideButton.text = resources.getString(R.string.stop)
            rightSideButton.visibility = View.VISIBLE
        }
    }
    private fun showPauseActionButtons(){
        binding.apply {
            leftSideButton.text = resources.getString(R.string.resume)
            rightSideButton.text = resources.getString(R.string.stop)
            rightSideButton.visibility = View.VISIBLE
        }
    }
    private fun showDeepPauseActionButtons(){
        binding.apply {
            rightSideButton.visibility = View.VISIBLE
            leftSideButton.text = resources.getString(R.string.save)
            rightSideButton.text = resources.getString(R.string.discard)
            continueRun.visibility = View.VISIBLE
        }
        zoomOutToCoverAll()
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
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
    }


}