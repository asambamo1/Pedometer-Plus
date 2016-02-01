package com.pedometer.asamapps.ActivitySteps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.pedometer.asamapps.R;
import com.pedometer.asamapps.Notifiers.CaloriesNotifier;
import com.pedometer.asamapps.Notifiers.DistanceNotifier;
import com.pedometer.asamapps.Notifiers.PaceNotifier;
import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.UtilsAndOthers.PedometerUtils;

import java.util.Timer;
import java.util.TimerTask;

public class StepService extends Service {
    private static final String TAG = "com.pedometer.asamapps.Steps.StepService";
    private final IBinder mBinder;
    private ICallback mCallback;
    private float mCalories;
    private CaloriesNotifier.Listener mCaloriesListener;
    private CaloriesNotifier mCaloriesNotifier;
    private int mDesiredPace;
    private float mDesiredSpeed;
    private float mDistance;
    private DistanceNotifier.Listener mDistanceListener;
    private DistanceNotifier mDistanceNotifier;
    private NotificationManager mNM;
    private int mPace;
    private PaceNotifier.Listener mPaceListener;
    private PaceNotifier mPaceNotifier;
    private PedometerSettings mPedometerSettings;
    private BroadcastReceiver mReceiver;
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private SharedPreferences mSettings;
    private SpeakingTimer mSpeakingTimer;
    private float mSpeed;
    private SpeedNotifier.Listener mSpeedListener;
    private SpeedNotifier mSpeedNotifier;
    private SharedPreferences mState;
    private Editor mStateEditor;
    private StepDetector mStepDetector;
    private StepDisplayer mStepDisplayer;
    private StepDisplayer.Listener mStepListener;
    private int mSteps;
    private long mTime;
    private PedometerUtils mPedometerUtils;
    private long start;
    private Timer timer;
    private TimerTask timerTask;
    private WakeLock wakeLock;


    public StepService() {
        this.start = -1;
        this.mBinder = new StepBinder();
        this.mStepListener = new StepDisplay();
        this.mPaceListener = new Pacenotifier();
        this.mDistanceListener = new Distnotifier();
        this.mSpeedListener = new Speednotifier();
        this.mCaloriesListener = new Caloriesnotifier();
        this.mReceiver = new BR();
    }

    private void stopTimer() {
        if (this.timer != null) {
            this.start = -1;
            this.timer.cancel();
            this.timer.purge();
        }
    }

    private void startTimer() {
        this.timer = new Timer();
        this.timerTask = new timetask();
        this.timer.schedule(this.timerTask, 1, 300);
    }

    public void onCreate() {
        super.onCreate();
        this.mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        this.mPedometerSettings = new PedometerSettings(this.mSettings);
        this.mState = getSharedPreferences("state", 0);
        this.mPedometerUtils = PedometerUtils.getInstance();
        this.mPedometerUtils.setService(this);
        this.mPedometerUtils.initTTS();
        acquireWakeLock();
        this.mStepDetector = new StepDetector();
        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerDetector();
        registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
        this.mStepDisplayer = new StepDisplayer(this.mPedometerSettings, this.mPedometerUtils);
        StepDisplayer stepDisplayer = this.mStepDisplayer;
        int i = this.mState.getInt("steps", 0);
        this.mSteps = i;
        stepDisplayer.setSteps(i);
        this.mStepDisplayer.addListener(this.mStepListener);
        this.mStepDetector.addStepListener(this.mStepDisplayer);
        this.mPaceNotifier = new PaceNotifier(this.mPedometerSettings, this.mPedometerUtils);
        PaceNotifier paceNotifier = this.mPaceNotifier;
        i = this.mState.getInt("pace", 0);
        this.mPace = i;
        paceNotifier.setPace(i);
        this.mPaceNotifier.addListener(this.mPaceListener);
        this.mStepDetector.addStepListener(this.mPaceNotifier);
        this.mDistanceNotifier = new DistanceNotifier(this.mDistanceListener, this.mPedometerSettings, this.mPedometerUtils);
        DistanceNotifier distanceNotifier = this.mDistanceNotifier;
        float f = this.mState.getFloat("distance", 0.0f);
        this.mDistance = f;
        distanceNotifier.setDistance(f);
        this.mStepDetector.addStepListener(this.mDistanceNotifier);
        this.mSpeedNotifier = new SpeedNotifier(this.mSpeedListener, this.mPedometerSettings, this.mPedometerUtils);
        SpeedNotifier speedNotifier = this.mSpeedNotifier;
        f = this.mState.getFloat("speed", 0.0f);
        this.mSpeed = f;
        speedNotifier.setSpeed(f);
        this.mPaceNotifier.addListener(this.mSpeedNotifier);
        mCaloriesNotifier = new CaloriesNotifier(mCaloriesListener, mPedometerSettings, mPedometerUtils);
        CaloriesNotifier caloriesNotifier = this.mCaloriesNotifier;
        f = this.mState.getFloat("calories", 0.0f);
        this.mCalories = f;
        caloriesNotifier.setCalories(f);
        this.mStepDetector.addStepListener(this.mCaloriesNotifier);
        this.mSpeakingTimer = new SpeakingTimer(this.mPedometerSettings, this.mPedometerUtils);
        this.mSpeakingTimer.addListener(this.mStepDisplayer);
        this.mSpeakingTimer.addListener(this.mPaceNotifier);
        this.mSpeakingTimer.addListener(this.mDistanceNotifier);
        this.mSpeakingTimer.addListener(this.mSpeedNotifier);
        this.mSpeakingTimer.addListener(this.mCaloriesNotifier);
        this.mStepDetector.addStepListener(this.mSpeakingTimer);
        reloadSettings();
        Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startTimer();
    }

    public void onDestroy() {
        this.mPedometerUtils.shutdownTTS();
        unregisterReceiver(this.mReceiver);
        unregisterDetector();
        stopTimer();
        this.mStateEditor = this.mState.edit();
        this.mStateEditor.putInt("steps", this.mSteps);
        this.mStateEditor.putInt("pace", this.mPace);
        this.mStateEditor.putFloat("distance", this.mDistance);
        this.mStateEditor.putFloat("speed", this.mSpeed);
        this.mStateEditor.putFloat("calories", this.mCalories);
        this.mStateEditor.putLong("time", this.mTime + this.mState.getLong("time", 0));
        this.mStateEditor.commit();
        this.mNM.cancel(R.string.app_name);
        this.wakeLock.release();
        super.onDestroy();
        this.mSensorManager.unregisterListener(this.mStepDetector);
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }

    private void registerDetector() {
        this.mSensor = this.mSensorManager.getDefaultSensor(1);
        this.mSensorManager.registerListener(this.mStepDetector, this.mSensor, 0);
    }

    private void unregisterDetector() {
        this.mSensorManager.unregisterListener(this.mStepDetector);
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void registerCallback(ICallback cb) {
        this.mCallback = cb;
    }

    public void setDesiredPace(int desiredPace) {
        this.mDesiredPace = desiredPace;
        if (this.mPaceNotifier != null) {
            this.mPaceNotifier.setDesiredPace(this.mDesiredPace);
        }
    }

    public void setDesiredSpeed(float desiredSpeed) {
        this.mDesiredSpeed = desiredSpeed;
        if (this.mSpeedNotifier != null) {
            this.mSpeedNotifier.setDesiredSpeed(this.mDesiredSpeed);
        }
    }

    public void reloadSettings() {
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        if (this.mStepDetector != null) {
            this.mStepDetector.setSensitivity(Float.valueOf(this.mSettings.getString("sensitivity", "6")).floatValue());
        }
        if (this.mStepDisplayer != null) {
            this.mStepDisplayer.reloadSettings();
        }
        if (this.mPaceNotifier != null) {
            this.mPaceNotifier.reloadSettings();
        }
        if (this.mDistanceNotifier != null) {
            this.mDistanceNotifier.reloadSettings();
        }
        if (this.mSpeedNotifier != null) {
            this.mSpeedNotifier.reloadSettings();
        }
        if (this.mCaloriesNotifier != null) {
            this.mCaloriesNotifier.reloadSettings();
        }
        if (this.mSpeakingTimer != null) {
            this.mSpeakingTimer.reloadSettings();
        }
    }

    public void resetValues() {
        this.mStepDisplayer.setSteps(0);
        this.mPaceNotifier.setPace(0);
        this.mDistanceNotifier.setDistance(0.0f);
        this.mSpeedNotifier.setSpeed(0.0f);
        this.mCaloriesNotifier.setCalories(0.0f);
        stopTimer();
        startTimer();
    }

    private void showNotification() {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.walking, null, System.currentTimeMillis());
        notification.flags = 34;
        Intent pedometerIntent = new Intent();
        pedometerIntent.setComponent(new ComponentName(this, PedomenterMainActivity.class));
        pedometerIntent.addFlags(67108864);
        notification.setLatestEventInfo(this, text, getText(R.string.notification_subtitle), PendingIntent.getActivity(this, 0, pedometerIntent, 0));
        this.mNM.notify(R.string.app_name, notification);
    }

    private void acquireWakeLock() {
        int wakeFlags;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (this.mPedometerSettings.wakeAggressively()) {
            wakeFlags = 268435462;
        } else if (this.mPedometerSettings.keepScreenOn()) {
            wakeFlags = 6;
        } else {
            wakeFlags = 1;
        }
        this.wakeLock = pm.newWakeLock(wakeFlags, TAG);
        this.wakeLock.acquire();
    }

    public interface ICallback {
        void caloriesChanged(float f);

        void distanceChanged(float f);

        void paceChanged(int i);

        void speedChanged(float f);

        void stepsChanged(int i);

        void timeChanged(long j);
    }

    class BR extends BroadcastReceiver {
        BR() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                StepService.this.unregisterDetector();
                StepService.this.registerDetector();
                if (StepService.this.mPedometerSettings.wakeAggressively()) {
                    StepService.this.wakeLock.release();
                    StepService.this.acquireWakeLock();
                }
            }
        }
    }

    class timetask extends TimerTask {
        timetask() {
        }

        public void run() {
            if (StepService.this.start == -1) {
                StepService.this.start = System.currentTimeMillis();
            }
            if (StepService.this.mCallback != null) {
                StepService.this.mTime = System.currentTimeMillis() - StepService.this.start;
                StepService.this.mCallback.timeChanged(StepService.this.mTime);
            }
        }
    }

    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    class StepDisplay implements StepDisplayer.Listener {
        StepDisplay() {
        }

        public void stepsChanged(int value) {
            StepService.this.mSteps = value;
            passValue();
        }

        public void passValue() {
            if (StepService.this.mCallback != null) {
                StepService.this.mCallback.stepsChanged(StepService.this.mSteps);
            }
        }
    }

    class Pacenotifier implements PaceNotifier.Listener {
        Pacenotifier() {
        }

        public void paceChanged(int value) {
            StepService.this.mPace = value;
            passValue();
        }

        public void passValue() {
            if (StepService.this.mCallback != null) {
                StepService.this.mCallback.paceChanged(StepService.this.mPace);
            }
        }
    }

    class Distnotifier implements DistanceNotifier.Listener {
        Distnotifier() {
        }

        public void valueChanged(float value) {
            StepService.this.mDistance = value;
            passValue();
        }

        public void passValue() {
            if (StepService.this.mCallback != null) {
                StepService.this.mCallback.distanceChanged(StepService.this.mDistance);
            }
        }
    }

    class Speednotifier implements SpeedNotifier.Listener {
        Speednotifier() {
        }

        public void valueChanged(float value) {
            StepService.this.mSpeed = value;
            passValue();
        }

        public void passValue() {
            if (StepService.this.mCallback != null) {
                StepService.this.mCallback.speedChanged(StepService.this.mSpeed);
            }
        }
    }

    class Caloriesnotifier implements CaloriesNotifier.Listener {
        Caloriesnotifier() {
        }

        public void valueChanged(float value) {
            StepService.this.mCalories = value;
            passValue();
        }

        public void passValue() {
            if (StepService.this.mCallback != null) {
                StepService.this.mCallback.caloriesChanged(StepService.this.mCalories);
            }
        }
    }
}