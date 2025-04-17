package com.scaredeer.intervalreload

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TAG = MainViewModel::class.java.simpleName

class MainViewModel : ViewModel() {

    private val _datetime: MutableStateFlow<String> = MutableStateFlow(currentDatetime())
    val datetime: StateFlow<String> = _datetime

    fun refresh() {
        //_datetime.value = currentDatetime()

        viewModelScope.launch {
            try {
                // Google の NTP サーバーから取得した時間
                val timeResponse = SNTPClient.getDate()
                _datetime.value = timeResponse.datetimeString
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
                return@launch
            }
        }
    }

    // 初期化時のみ利用するシステム時間
    private fun currentDatetime(): String {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(SNTPClient.DATE_FORMAT))
    }

    private val _isTimerActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive

    private val _buttonLabel: MutableStateFlow<String> = MutableStateFlow("inactivate")
    val buttonLabel: StateFlow<String> = _buttonLabel

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