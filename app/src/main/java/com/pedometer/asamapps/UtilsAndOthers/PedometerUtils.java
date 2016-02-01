package com.pedometer.asamapps.UtilsAndOthers;

import java.util.Locale;

import android.app.Service;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.util.Log;

public class PedometerUtils implements TextToSpeech.OnInitListener {
    private static final String TAG = "PedometerUtils";
    private Service mService;

    private static PedometerUtils instance = null;

    private PedometerUtils() {
    }
     
    public static PedometerUtils getInstance() {
        if (instance == null) {
            instance = new PedometerUtils();
        }
        return instance;
    }
    
    public void setService(Service service) {
        mService = service;
    }
    
    /********** WHEN SPEAKING **********/
    
    private TextToSpeech mTts;
    private boolean mSpeak = false;
    private boolean mSpeakingEngineAvailable = false;

    public void initTTS() {
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        Log.i(TAG, "Initializing TextToSpeech...");
        mTts = new TextToSpeech(mService,
            this  // TextToSpeech.OnInitListener
            );
    }
    public void shutdownTTS() {
        Log.i(TAG, "Shutting Down TextToSpeech...");

        mSpeakingEngineAvailable = false;
        mTts.shutdown();
        Log.i(TAG, "TextToSpeech Shut Down.");

    }
    public void say(String text) {
        if (mSpeak && mSpeakingEngineAvailable) {
            mTts.speak(text,
                    TextToSpeech.QUEUE_ADD,  // Drop all pending entries in the playback queue.
                    null);
        }
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                Log.i(TAG, "TextToSpeech Initialized.");
                mSpeakingEngineAvailable = true;
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    public void setSpeak(boolean speak) {
        mSpeak = speak;
    }

    public boolean isSpeakingEnabled() {
        return mSpeak;
    }

    public boolean isSpeakingNow() {
        return mTts.isSpeaking();
    }

    public void ding() {
    }
    
    /********** Time **********/
    
    public static long currentTimeInMillis() {
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }

    public static String formatTime(long time) {
        Object obj;
        long sec = (time / 1000) % 60;
        long min = (time / 60000) % 60;
        if ((time / 3600000) % 24 == 0) {
            obj = "";
        } else {
            obj = String.format("%02d:", new Object[]{Long.valueOf((time / 3600000) % 24)});
        }
        return new StringBuilder(String.valueOf(obj)).append(String.format("%02d:%02d", new Object[]{Long.valueOf(min), Long.valueOf(sec)})).toString();
    }
}
