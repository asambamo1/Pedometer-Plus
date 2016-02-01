

package com.pedometer.asamapps.ActivitySteps;

import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.UtilsAndOthers.PedometerUtils;

import java.util.ArrayList;


public class StepDisplayer implements StepListener, SpeakingTimer.Listener {

    private int mCount = 0;
    PedometerSettings mSettings;
    PedometerUtils mPedometerUtils;

    public StepDisplayer(PedometerSettings settings, PedometerUtils pedometerUtils) {
        mPedometerUtils = pedometerUtils;
        mSettings = settings;
        notifyListener();
    }
    public void setUtils(PedometerUtils pedometerUtils) {
        mPedometerUtils = pedometerUtils;
    }

    public void setSteps(int steps) {
        mCount = steps;
        notifyListener();
    }
    public void onStep() {
        mCount ++;
        notifyListener();
    }
    public void reloadSettings() {
        notifyListener();
    }
    public void passValue() {
    }
    
    

    //-----------------------------------------------------
    // Listener
    
    public interface Listener {
        public void stepsChanged(int value);
        public void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }
    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged((int)mCount);
        }
    }
    
    //-----------------------------------------------------
    // Speaking
    
    public void speak() {
        if (mSettings.shouldTellSteps()) { 
            if (mCount > 0) {
                mPedometerUtils.say("" + mCount + " steps");
            }
        }
    }
    
    
}
