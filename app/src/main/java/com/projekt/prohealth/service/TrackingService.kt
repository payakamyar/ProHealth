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
import com.projekt.prohealth.R
import com.projekt.prohealth.entity.TimeData
import com.projekt.prohealth.ui.activity.MainActivity
import com.projekt.prohealth.utility.Constants.ACTION_DEEP_PAUSE_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_OPEN_TRACKING_FRAGMENT
import com.projekt.prohealth.utility.Constants.ACTION_PAUSE_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_START_OR_RESUME_SERVICE
import com.projekt.prohealth.utility.Constants.ACTION_STOP_SERVICE
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_ID
import com.projekt.prohealth.utility.Constants.NOTIFICATION_CHANNEL_NAME
import com.projekt.prohealth.utility.Constants.NOTIFICATION_ID
import com.projekt.prohealth.utility.Utilities
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class TrackingService: LifecycleService() {


    companion object{
        var isServiceRunning = false
        var currentState = MutableLiveData(ACTION_STOP_SERVICE)
        var route = MutableLiveData<MutableList<GeoPoint>>(mutableListOf())
        var time = MutableLiveData(TimeData(0,Utilities.formatTime(0)))
        var distance:Double = 0.0
        var averageSpeed:Double = 0.0
        var caloriesBurned = 0
    }
    private var isNotificationInitialized = false
    private lateinit var drawPathLocationListener: LocationListener
    private lateinit var locationManager: LocationManager
    private lateinit var notificationBuilder:NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var resumeAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var stopAction: NotificationCompat.Action
    private lateinit var timerJob:Job

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate() {
        super.onCreate()
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
            GeoPoint(latitude,longitude).apply {
                if(route.value!!.isNotEmpty() && isServiceRunning){
                    distance += this.distanceToAsDouble(route.value!!.last())
                    caloriesBurned = calculateCaloriesBurned()
                    val avgSpeed = (distance.div(1000.0)).div(time.value!!.second.toDouble().div(3600.0))
                    averageSpeed = if(avgSpeed.isInfinite() || avgSpeed.isNaN())
                        0.00
                    else
                        avgSpeed
                }
                if(!isServiceRunning)
                    isServiceRunning = true
                route.value!!.add(this)
            }
            currentState.value = ACTION_START_OR_RESUME_SERVICE
            route.postValue(route.value!!)
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(!isNotificationInitialized){
                        startForegroundService()
                        isNotificationInitialized = true
                    }
                    if(currentState.value!! != ACTION_START_OR_RESUME_SERVICE){
                            startTracking()
                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    if(currentState.value!! == ACTION_START_OR_RESUME_SERVICE){
                        currentState.postValue(ACTION_PAUSE_SERVICE)
                        updateNotificationActions(ACTION_PAUSE_SERVICE)
                        stopTracking()
                    }
                }
                ACTION_DEEP_PAUSE_SERVICE -> {
                        currentState.postValue(ACTION_DEEP_PAUSE_SERVICE)
                        updateNotificationActions(ACTION_DEEP_PAUSE_SERVICE)
                        stopTracking()
                }
                ACTION_STOP_SERVICE -> {
                    stopTracking()
                    currentState.postValue(ACTION_STOP_SERVICE)
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
            .setContentText(time.value!!.formattedTimeToString)
            .setContentIntent(getMainActivity())



        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(){
        if(Utilities.checkLocationPermissions(this)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,0f,drawPathLocationListener)
            currentState.postValue(ACTION_START_OR_RESUME_SERVICE)
            updateNotificationActions(ACTION_START_OR_RESUME_SERVICE)
            startTimer()
        }
    }

    private fun stopTracking(){
        isServiceRunning = false
        timerJob.cancel()
        locationManager.removeUpdates(drawPathLocationListener)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun startTimer(){
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (currentState.value!! == ACTION_START_OR_RESUME_SERVICE){
                delay(1000)
                val newTime = (time.value!!.second)+1
                time.value = TimeData(newTime,Utilities.formatTime(newTime))
                updateNotificationTimer(time.value!!.formattedTimeToString)
            Log.i("working", time.value!!.formattedTimeToString)
            }}
    }

    private fun calculateCaloriesBurned():Int{
        if(time.value!!.second != 0 && averageSpeed > 0){
        val met = (3.5 + (0.1 * averageSpeed.toInt()))
        return  met.times(80).times(time.value!!.second.div(60)).toInt()
        }
        return 0
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
                notificationBuilder.addAction(stopAction)
            }
            ACTION_PAUSE_SERVICE -> {
                notificationBuilder.addAction(resumeAction)
                notificationBuilder.addAction(stopAction)
            }
            ACTION_DEEP_PAUSE_SERVICE -> {}
        }
        notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build())
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
        currentState.value = ACTION_STOP_SERVICE
        isServiceRunning = false
        time.value = TimeData(0,Utilities.formatTime(0))
        distance = 0.0
        averageSpeed = 0.0
        caloriesBurned = 0
        notificationManager.cancel(NOTIFICATION_ID)
        locationManager.removeUpdates(drawPathLocationListener)
    }
}