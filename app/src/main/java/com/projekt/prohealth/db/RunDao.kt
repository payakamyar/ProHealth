package com.projekt.prohealth.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projekt.prohealth.entity.Run

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRun(run: Run)

    @Delete
    fun deleteRun(run: Run)


    @Query("SELECT * FROM running_tbl ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_tbl ORDER BY time DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_tbl ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_tbl ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAverageSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_tbl ORDER BY distanceInMeter DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(time) FROM running_tbl")
    fun getTotalRunInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_tbl")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeter) FROM running_tbl")
    fun getTotalDistance(): LiveData<Long>

    @Query("SELECT AVG(avgSpeedInKMH) FROM running_tbl")
    fun getAverageSpeed(): LiveData<Float>

}