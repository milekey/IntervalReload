package com.scaredeer.intervalreload;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<String> mDatetime;
    public LiveData<String> getDatetime() {
        return mDatetime;
    }

    public final MutableLiveData<Boolean> isTimerActive;

    public MainViewModel() {
        mDatetime = new MutableLiveData<>(currentDatetime());
        isTimerActive = new MutableLiveData<>(false);
    }

    void postRefresh() {
        mDatetime.postValue(currentDatetime());
    }

    private String currentDatetime() {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L)
                .atZone(ZoneId.of("JST"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}