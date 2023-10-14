package com.projekt.prohealth.viewmodel

import androidx.lifecycle.ViewModel
import com.projekt.prohealth.db.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    val repository: MainRepository
                                            ): ViewModel() {



}