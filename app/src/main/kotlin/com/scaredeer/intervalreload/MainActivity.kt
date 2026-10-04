package com.scaredeer.intervalreload

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.scaredeer.intervalreload.ui.MainScreen
import com.scaredeer.intervalreload.ui.MainViewModel
import com.scaredeer.intervalreload.ui.theme.AppTheme

private val TAG = MainActivity::class.java.simpleName

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")

        if (viewModel.isTimerActive) {
            viewModel.startTimer()
        }
    }

    override fun onPause() {
        Log.v(TAG, "onPause")
        if (viewModel.isTimerActive) {
            viewModel.pauseTimer()
        }

        super.onPause()
    }
}