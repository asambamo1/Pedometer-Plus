
package com.pedometer.asamapps.Notifiers;


import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.ActivitySteps.SpeakingTimer;
import com.pedometer.asamapps.ActivitySteps.StepListener;
import com.pedometer.asamapps.UtilsAndOthers.PedometerUtils;

public class DistanceNotifier implements StepListener, SpeakingTimer.Listener {

    public interface Listener {
        public void valueChanged(float value);
        public void passValue();
    }
    private Listener mListener;
    
    float mDistance = 0;
    
    PedometerSettings mSettings;
    PedometerUtils mPedometerUtils;
    
    boolean mIsMetric;
    float mStepLength;

    public DistanceNotifier(Listener listener, PedometerSettings settings, PedometerUtils pedometerUtils) {
        mListener = listener;
        mPedometerUtils = pedometerUtils;
        mSettings = settings;
        reloadSettings();
    }
    public void setDistance(float distance) {
        mDistance = distance;
        notifyListener();
    }
    
    public void reloadSettings() {
        mIsMetric = mSettings.isMetric();
        mStepLength = mSettings.getStepLength();
        notifyListener();
    }
    
    public void onStep() {
        
        if (mIsMetric) {
            mDistance += (float)(// kilometers
                mStepLength // centimeters
                / 100000.0); // centimeters/kilometer
        }
        else {
            mDistance += (float)(// miles
                mStepLength // inches
                / 63360.0); // inches/mile
        }
        
        notifyListener();
    }
    
    private void notifyListener() {
        mListener.valueChanged(mDistance);
    }
    
    public void passValue() {
        // Callback of StepListener - Not implemented
    }

    public void speak() {
        if (mSettings.shouldTellDistance()) {
            if (mDistance >= .001f) {
                mPedometerUtils.say(("" + (mDistance + 0.000001f)).substring(0, 4) + (mIsMetric ? " kilometers" : " miles"));
                // TODO: format numbers (no "." at the end)
            }
        }
    }
    

}

