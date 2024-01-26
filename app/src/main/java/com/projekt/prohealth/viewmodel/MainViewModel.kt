package com.projekt.prohealth.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.TypeConverter
import com.projekt.prohealth.db.repository.FirebaseRepository
import com.projekt.prohealth.db.repository.MainRepository
import com.projekt.prohealth.entity.Run
import com.projekt.prohealth.utility.Utilities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor():ViewModel() {



    @Inject
    lateinit var repository: MainRepository
    @Inject
    lateinit var remoteRepository: FirebaseRepository

    fun insertRun(run:Run) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertRun(run)
        remoteRepository.addRun(run)
    }
    fun getRuns():LiveData<List<Run>> {
        var data: MutableLiveData<List<Run>> =  MutableLiveData<List<Run>>()
        viewModelScope.launch {
            data = try {
                remoteRepository.getRuns() as MutableLiveData<List<Run>>
            }catch (e:Exception){
                repository.getAllRunsSortedByDate() as MutableLiveData<List<Run>>
            }
        }
        return data
    }


}