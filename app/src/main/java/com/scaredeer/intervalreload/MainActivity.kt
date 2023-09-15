package com.scaredeer.intervalreload

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.scaredeer.intervalreload.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // https://developer.android.com/topic/libraries/data-binding/architecture
        // https://developer.android.com/topic/libraries/data-binding/architecture#livedata
        // https://developer.android.com/topic/libraries/data-binding/architecture#viewmodel
        val binding =
            DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.button.setOnClickListener {
            if (viewModel.isTimerActive()) {
                stopTimer()
                binding.button.text = "activate"
            } else {
                startTimer()
                binding.button.text = "inactivate"
            }
        }

        handler = Handler(mainLooper)
        runnable = Runnable {
            viewModel.refresh()
            handler.postDelayed(runnable, 1000L)
        }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isTimerActive()) {
            startTimer()
        }
    }

    override fun onPause() {
        if (viewModel.isTimerActive()) {
            pauseTimer()
        }

        super.onPause()
    }

    private fun startTimer() {
        stopTimer() // スレッドの多重起動を防ぐため、
        // 既存のタイマーがあったとしたらちゃんと終了してから以下の処理に臨むようにする

        handler.post(runnable)

        viewModel.setIsTimerActive(true)
    }

    // ユーザーの意思でタイマーを停止したので、当然、isTimerActive も false にトグルさせる。
    private fun stopTimer() {
        pauseTimer()

        viewModel.setIsTimerActive(false)
    }

    // 単なる pause の時は（復帰時にタイマーを再スタートする必要があるので）
    // isTimerActive は false にトグルさせない。
    private fun pauseTimer() {
        handler.removeCallbacks(runnable)
    }
}