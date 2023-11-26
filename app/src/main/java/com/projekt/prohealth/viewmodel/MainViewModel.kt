package com.projekt.prohealth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.projekt.prohealth.db.repository.MainRepository
import com.projekt.prohealth.entity.Run
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor():ViewModel() {

    @Inject
    lateinit var repository: MainRepository

    private val _data: MutableLiveData<List<Run>> = MutableLiveData()
    val data:LiveData<List<Run>> = _data

    init {
        _data.value = getRuns()
    }
    fun getRuns():List<Run>{
        return listOf()
    }

}