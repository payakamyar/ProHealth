package com.projekt.prohealth.db.repository

import androidx.lifecycle.LiveData
import com.projekt.prohealth.entity.Run
import com.projekt.prohealth.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao:RunDao
) {

    fun insertRun(run: Run) = runDao.insertRun(run)

    fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate(): LiveData<List<Run>> = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>> = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>> = runDao.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAverageSpeed(): LiveData<List<Run>> = runDao.getAllRunsSortedByAverageSpeed()

    fun getAllRunsSortedByDistance(): LiveData<List<Run>> = runDao.getAllRunsSortedByDistance()

    fun getTotalRunInMillis(): LiveData<Long> = runDao.getTotalRunInMillis()

    fun getTotalCaloriesBurned(): LiveData<Int> = runDao.getTotalCaloriesBurned()

    fun getTotalDistance(): LiveData<Long> = runDao.getTotalDistance()

    fun getAverageSpeed(): LiveData<Float> = runDao.getAverageSpeed()

}