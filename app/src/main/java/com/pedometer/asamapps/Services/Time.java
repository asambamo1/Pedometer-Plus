package com.pedometer.asamapps.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.pedometer.asamapps.R;

public class Time extends Service {


    long timeInMillies = 0L;
    long timeSwap = 0L;
    long finalTime = 0L;
    public Runnable updateTimerMethod;
    private long startTime = 0L;
    private int seconds;
    private int minutes;
    private int hours;
    private TextView timer;
    private Handler myHandler = new Handler();

        @Override
        public IBinder onBind(Intent i) {
            return null;
        }

        @Override
        public int onStartCommand(Intent i, int flags, int startId) {
            Runnable updateTimerMethod = new Runnable() {

                public void run() {
                    timer = (TextView) timer.findViewById(R.id.time);
                    timeInMillies = SystemClock.uptimeMillis() - startTime;
                    finalTime = timeSwap + timeInMillies;
                    seconds = (int) (finalTime / 1000);
                    minutes = seconds / 60;
                    hours = minutes / 60;
                    seconds = seconds % 60;
                    minutes = minutes % 60;
                    int milliseconds = (int) (finalTime % 1000);
                    timer.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":"
                            + String.format("%02d", seconds));
                    myHandler.postDelayed(this, 0);
                }


            };
            return super.onStartCommand(i, flags, startId);
        }
    }
