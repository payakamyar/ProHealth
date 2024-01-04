package com.projekt.prohealth.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_tbl")
data class Run(
    var img: Bitmap?,
    var timestamp: Long,
    var avgSpeedInKMH: Float,
    var distanceInMeter: Double,
    var time: Long,
    var caloriesBurned: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
