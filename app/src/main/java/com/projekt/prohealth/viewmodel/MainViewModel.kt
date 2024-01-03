package com.projekt.prohealth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projekt.prohealth.db.repository.MainRepository
import com.projekt.prohealth.entity.Run
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor():ViewModel() {

    @Inject
    lateinit var repository: MainRepository

    fun insertRun(run:Run) = viewModelScope.launch {repository.insertRun(run)}
    

}