package com.scaredeer.intervalreload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    private val _datetime: MutableLiveData<String> = MutableLiveData(currentDatetime())
    val datetime: LiveData<String>
        get() = _datetime

    fun refresh() {
        _datetime.postValue(currentDatetime())
    }

    private val isTimerActive: MutableLiveData<Boolean> = MutableLiveData(false)
    fun isTimerActive(): Boolean {
        return isTimerActive.value!!
    }

    fun setIsTimerActive(isActive: Boolean) {
        isTimerActive.value = isActive
    }

    private fun currentDatetime(): String {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L)
            .atZone(ZoneId.of("JST"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}