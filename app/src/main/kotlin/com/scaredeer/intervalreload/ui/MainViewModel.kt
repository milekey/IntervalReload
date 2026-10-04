package com.scaredeer.intervalreload.ui

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaredeer.intervalreload.SNTPClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * cf. https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state
 */
private val TAG = MainViewModel::class.java.simpleName

data class UiState(
    val datetime: String = "",
    val buttonLabel: String = "activate",
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var isTimerActive: Boolean

    private var handler: Handler
    private var runnable: Runnable

    fun toggleTimer() {
        _uiState.update { currentState ->
            if (isTimerActive) {
                stopTimer()
                isTimerActive = false
                currentState.copy(buttonLabel = "activate")
            } else {
                startTimer()
                isTimerActive = true
                currentState.copy(buttonLabel = "inactivate")
            }
        }
    }
    fun update() {
        viewModelScope.launch {
            try {
                // Google の NTP サーバーから取得した時間
                val timeResponse = SNTPClient.getDate()
                _uiState.update { currentState ->
                    currentState.copy(datetime = timeResponse.datetimeString)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
                return@launch
            }
        }
    }

    fun startTimer() {
        stopTimer() // スレッドの多重起動を防ぐため、
        // 既存のタイマーがあったとしたらちゃんと終了してから以下の処理に臨むようにする

        handler.post(runnable)
    }

    fun stopTimer() {
        pauseTimer()
    }

    // onStop ではない、単なる onPause の時は（復帰時にタイマーを再スタートする必要があるので）
    // isTimerActive は false にトグルされない。
    fun pauseTimer() {
        handler.removeCallbacks(runnable)
    }

    // 初期化時のみ利用するシステム時間を返す関数
    private fun currentDatetime(): String {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(SNTPClient.DATE_FORMAT))
    }

    init {
        _uiState.value = UiState(datetime = currentDatetime())

        isTimerActive = false

        // もちろん、Kotlin の coroutine と delay を使えば、このように Android OS のタスクキューを使った
        // Handler.postDelayed を使わずとも、同様のことは実現できる
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            update()
            handler.postDelayed(runnable, 1000L)
        }
    }

    override fun onCleared() {
        Log.v(TAG, "onCleared")
        super.onCleared()
    }
}