package com.projekt.prohealth.db.repository

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projekt.prohealth.entity.Run
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    private val runList: MutableLiveData<List<Run>> = MutableLiveData<List<Run>>()
    private val result:LiveData<List<Run>> get() = runList

    init {
        runList.value = mutableListOf()
    }
    fun getRuns():LiveData<List<Run>>{
        auth.currentUser?.let {
            firestore.collection("users").document(it.uid).collection("runs").get()
                .addOnSuccessListener {query ->
                    val runs:MutableList<Run> = mutableListOf()
                query.forEach {docs ->
                    docs?.let { that ->
                        val myRun = Run(avgSpeedInKMH = (that.get("avgSpeedInKMH") as Double).toFloat(),
                            caloriesBurned = (that.get("caloriesBurned") as Long).toInt(),
                            img = Base64.decode(that.get("img") as String,Base64.DEFAULT),
                            time = that.get("time") as Long,
                            distanceInMeter = that.get("distanceInMeter") as Double,
                            timestamp = that.get("timestamp") as Long)
                        runs.add(myRun)
                    }
                    runList.value = runs
                }
            }
        }
        return result
    }
    fun addRun(run:Run):Int{
        var size = 0
        var code = 400
        auth.currentUser?.let {

            firestore.collection("users").document(it.uid).collection("runs").get()
                .addOnSuccessListener {query ->
                    size = query.documents.size
                    Log.i("size", "addRun: $size")
                }
            firestore.collection("users").document(it.uid).collection("runs").add(mapOf(
                "id" to (size + 1),
                "img" to Base64.encodeToString(run.img,Base64.DEFAULT),
                "timestamp" to run.timestamp,
                "avgSpeedInKMH" to run.avgSpeedInKMH,
                "distanceInMeter" to run.distanceInMeter,
                "time" to run.time,
                "caloriesBurned" to run.caloriesBurned
            )).addOnSuccessListener {
                code = 200
            }
        }
        return code
    }
}