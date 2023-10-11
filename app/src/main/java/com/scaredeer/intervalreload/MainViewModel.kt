package com.scaredeer.intervalreload

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TAG = MainViewModel::class.java.simpleName

class MainViewModel : ViewModel() {

    private val _datetime: MutableLiveData<String> = MutableLiveData(currentDatetime())
    val datetime: LiveData<String> = _datetime

    fun refresh() {
        _datetime.postValue(currentDatetime())
    }

    private fun currentDatetime(): String {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L)
            .atZone(ZoneId.of("JST"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    private val _isTimerActive: MutableLiveData<Boolean> = MutableLiveData(false)
    val isTimerActive: LiveData<Boolean> = _isTimerActive

    private val _buttonLabel: MutableLiveData<String> = MutableLiveData("inactivate")
    val buttonLabel: LiveData<String> = _buttonLabel

    fun startTimer() {
        _isTimerActive.value = true
        _buttonLabel.value = "inactivate"
    }
    fun stopTimer() {
        _isTimerActive.value = false
        _buttonLabel.value = "activate"
    }

    override fun onCleared() {
        Log.v(TAG, "onCleared")
        super.onCleared()
    }
}