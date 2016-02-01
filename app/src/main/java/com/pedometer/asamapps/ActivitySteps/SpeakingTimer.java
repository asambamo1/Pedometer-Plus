
package com.pedometer.asamapps.ActivitySteps;

import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.UtilsAndOthers.PedometerUtils;

import java.util.ArrayList;

public class SpeakingTimer implements StepListener {

    PedometerSettings mSettings;
    PedometerUtils mPedometerUtils;
    boolean mShouldSpeak;
    float mInterval;
    long mLastSpeakTime;
    
    public SpeakingTimer(PedometerSettings settings, PedometerUtils pedometerUtils) {
        mLastSpeakTime = System.currentTimeMillis();
        mSettings = settings;
        mPedometerUtils = pedometerUtils;
        reloadSettings();
    }
    public void reloadSettings() {
        mShouldSpeak = mSettings.shouldSpeak();
        mInterval = mSettings.getSpeakingInterval();
    }
    
    public void onStep() {
        long now = System.currentTimeMillis();
        long delta = now - mLastSpeakTime;
        
        if (delta / 60000.0 >= mInterval) {
            mLastSpeakTime = now;
            notifyListeners();
        }
    }
    
    public void passValue() {
        // not used
    }

    
    //-----------------------------------------------------
    // Listener
    
    public interface Listener {
        public void speak();
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }
    public void notifyListeners() {
        mPedometerUtils.ding();
        for (Listener listener : mListeners) {
            listener.speak();
        }
    }

    //-----------------------------------------------------
    // Speaking
    
    public boolean isSpeaking() {
        return mPedometerUtils.isSpeakingNow();
    }
}

