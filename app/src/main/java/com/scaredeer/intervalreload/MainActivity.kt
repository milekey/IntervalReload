package com.scaredeer.intervalreload

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scaredeer.intervalreload.databinding.MainActivityBinding

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")

        // https://developer.android.com/topic/libraries/data-binding/architecture
        // https://developer.android.com/topic/libraries/data-binding/architecture#livedata
        // https://developer.android.com/topic/libraries/data-binding/architecture#viewmodel
        val binding =
            DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        binding.lifecycleOwner = this // LiveData/StateFlow の変化を dataBinding から看視する場合に必要
        binding.viewModel = viewModel
        binding.button.setOnClickListener {
            if (viewModel.isTimerActive.value) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        // もちろん、Kotlin の coroutine と delay を使えば、このように Android OS のタスクキューを使った
        // Handler.postDelayed を使わずとも、同様のことは実現できる
        handler = Handler(mainLooper)
        runnable = Runnable {
            viewModel.refresh()
            handler.postDelayed(runnable, 1000L)
        }
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")

        if (viewModel.isTimerActive.value) {
            startTimer()
        }
    }

    override fun onPause() {
        Log.v(TAG, "onPause")
        if (viewModel.isTimerActive.value) {
            pauseTimer()
        }

        super.onPause()
    }

    private fun startTimer() {
        stopTimer() // スレッドの多重起動を防ぐため、
        // 既存のタイマーがあったとしたらちゃんと終了してから以下の処理に臨むようにする

        handler.post(runnable)

        viewModel.startTimer()
    }

    // ユーザーの意思でタイマーを停止したので、当然、isTimerActive も false にトグルさせる。
    private fun stopTimer() {
        pauseTimer()

        viewModel.stopTimer()
    }

    // 単なる pause の時は（復帰時にタイマーを再スタートする必要があるので）
    // viewModel.isTimerActive は false にトグルさせない。
    private fun pauseTimer() {
        handler.removeCallbacks(runnable)
    }
}