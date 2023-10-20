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
import com.projekt.prohealth.entity.TimeData
import com.projekt.prohealth.ui.activity.MainActivity
import com.projekt.prohealth.utility.Constants.ACTION_OPEN_TRACKING_FRAGMENT
import com.projekt.prohealth.utility.Constants.ACTION_PAUSE_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_START_OR_RESUME_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_STOP_SERVICE
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_ID
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_NAME
import com.projekt.prohealth.utility.Constants.NOTIFICATION_ID
import com.projekt.prohealth.utility.LocationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingService: LifecycleService() {

    //TODO: The notification actions aren't synced with the UI. Change it so when an Action is clicked on the notification, the UI change as well.

    companion object{
        var isTracking = MutableLiveData<Boolean>()
        var route = MutableLiveData<MutableList<MutableList<LatLng>>>()
        var time = MutableLiveData<TimeData>()
    }
    private var isFirstRun = true
    private lateinit var drawPathLocationListener: LocationListener
    private lateinit var locationManager: LocationManager
    private lateinit var notificationBuilder:NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var resumeAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var stopAction: NotificationCompat.Action

    override fun onCreate() {
        super.onCreate()
        isTracking.value = false
        route.postValue(mutableListOf())
        time.value = TimeData(0,"00:00:00")
        resumeAction =  NotificationCompat.Action(R.drawable.ic_play,"Resume",
                        PendingIntent.getService(this,9876,
                        Intent(this,TrackingService::class.java).apply { action = ACTION_START_OR_RESUME_SERVICE }, FLAG_UPDATE_CURRENT))
        pauseAction =  NotificationCompat.Action(R.drawable.ic_pause,"Pause",
                        PendingIntent.getService(this,9875,
                        Intent(this,TrackingService::class.java).apply { action = ACTION_PAUSE_SERVICE }, FLAG_UPDATE_CURRENT))
        stopAction =  NotificationCompat.Action(R.drawable.ic_stop,"Stop",
                        PendingIntent.getService(this,9874,
                        Intent(this,TrackingService::class.java).apply { action = ACTION_STOP_SERVICE }, FLAG_UPDATE_CURRENT))
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
                    if(!isTracking.value!!){
                        startTracking()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    if(isTracking.value!!){
                        stopTracking()
                        updateNotificationActions(ACTION_PAUSE_SERVICE)
                    }
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
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager)

       notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
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
            startTimer()
        }
    }

    private fun stopTracking(){
        isTracking.value = false
        updateNotificationActions(ACTION_START_OR_RESUME_SERVICE)
        locationManager.removeUpdates(drawPathLocationListener)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun startTimer(){
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                delay(1000)
                time.postValue(TimeData(time.value!!.second+1,formatTime(time.value!!.second)))
                updateNotificationTimer(time.value!!.formattedTimeToString)
                updateNotificationActions(ACTION_START_OR_RESUME_SERVICE)
            Log.i("working", "startTimer: ")
            }}
        Log.i("istracking", "${isTracking.value!!}")
    }

    private fun updateNotificationTimer(text:String){
        notificationBuilder.setContentText(text)
        notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun updateNotificationActions(action:String){
        notificationBuilder.clearActions()
        when(action){
            ACTION_START_OR_RESUME_SERVICE -> {
                notificationBuilder.addAction(pauseAction)
            }
            ACTION_PAUSE_SERVICE -> {
                notificationBuilder.addAction(resumeAction)
            }
        }
        notificationBuilder.addAction(stopAction)
        notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun formatTime(timeInSec:Int):String{
        if(timeInSec > 0){
            val sec = timeInSec % 60
            val min = (timeInSec / 60) % 60
            val hour = (timeInSec / 3600)
            return "${if (hour<10) "0$hour" else "$hour"}:${if (min<10) "0$min" else "$min"}:${if(sec<10) "0$sec" else "$sec"}"
        }
        return ""
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