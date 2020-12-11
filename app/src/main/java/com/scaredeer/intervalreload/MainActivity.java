package com.scaredeer.intervalreload;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.scaredeer.intervalreload.databinding.MainActivityBinding;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mViewModel;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mViewModel);
        binding.button.setOnClickListener(view -> {
            if (mViewModel.isTimerActive.getValue()) {
                stopTimer();
            } else {
                startTimer();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mViewModel.isTimerActive.getValue()) {
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        if (mViewModel.isTimerActive.getValue()) {
            pauseTimer();
        }

        super.onPause();
    }

    private void startTimer() {
        stopTimer(); // スレッドの多重起動を防ぐため、
        // 既存のタイマーがあったとしたらちゃんと終了してから以下の処理に臨むようにする

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mViewModel.postRefresh();
            }
        }, 0, 1000L);

        mViewModel.isTimerActive.setValue(true);
    }

    // ユーザーの意思でタイマーを停止したので、当然、isTimerActive も false にトグルさせる。
    private void stopTimer() {
        pauseTimer();

        mViewModel.isTimerActive.setValue(false);
    }

    // 単なる pause の時は（復帰時にタイマーを再スタートする必要があるので）
    // isTimerActive は false にトグルさせない。
    private void pauseTimer() {
        if (Objects.nonNull(mTimer)) {
            mTimer.cancel();
        }
    }
}