package com.projekt.prohealth.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_tbl")
data class Run(
    var img: ByteArray?,
    var timestamp: Long,
    var avgSpeedInKMH: Float,
    var distanceInMeter: Double,
    var time: Long,
    var caloriesBurned: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Run

        if (img != null) {
            if (other.img == null) return false
            if (!img.contentEquals(other.img)) return false
        } else if (other.img != null) return false
        if (timestamp != other.timestamp) return false
        if (avgSpeedInKMH != other.avgSpeedInKMH) return false
        if (distanceInMeter != other.distanceInMeter) return false
        if (time != other.time) return false
        if (caloriesBurned != other.caloriesBurned) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = img?.contentHashCode() ?: 0
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + avgSpeedInKMH.hashCode()
        result = 31 * result + distanceInMeter.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + caloriesBurned
        result = 31 * result + (id ?: 0)
        return result
    }
}
