package com.projekt.prohealth.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.projekt.prohealth.R
import com.projekt.prohealth.ui.activity.MainActivity
import com.projekt.prohealth.utility.Constants.ACTION_OPEN_TRACKING_FRAGMENT
import com.projekt.prohealth.utility.Constants.ACTION_PAUSE_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_START_OR_RESUME_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_STOP_SERVICE
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_ID
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_NAME
import com.projekt.prohealth.utility.Constants.NOTIFICATION_ID
import com.projekt.prohealth.utility.LocationPermission

class TrackingService: LifecycleService() {

    companion object{
        var isTracking = MutableLiveData<Boolean>()
        var route = MutableLiveData<MutableList<MutableList<LatLng>>>()
    }
    private var isFirstRun = true
    private lateinit var drawPathLocationListener: LocationListener
    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        isTracking.value = false
        route.postValue(mutableListOf())
        drawPathLocationListener = LocationListener {
            val latitude = it.latitude
            val longitude = it.longitude
            route.value!!.apply {
                if(!isTracking.value!!){
                    this.add(mutableListOf())
                    this.last().add(LatLng(latitude,longitude))
                    isTracking.value = true
                }else
                    this.last().add(LatLng(latitude,longitude))
                route.postValue(this)
            }
            Log.i("TAG", "${route.value!!.last().last().latitude}")
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.i("tracking", "${isTracking.value}")
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }
                    if(!isTracking.value!!)
                        startTracking()
                }
                ACTION_PAUSE_SERVICE -> {
                    if(isTracking.value!!)
                        stopTracking()
                }
                ACTION_STOP_SERVICE -> {
                    stopTracking()
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Tracking Run...")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivity())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(){
        if(LocationPermission.checkLocationPermissions(this)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,0f,drawPathLocationListener)
        }
    }

    private fun stopTracking(){
        isTracking.value = false
        locationManager.removeUpdates(drawPathLocationListener)
    }

    private fun getMainActivity() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_OPEN_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager:NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        isTracking.value = false
        locationManager.removeUpdates(drawPathLocationListener)
    }
}