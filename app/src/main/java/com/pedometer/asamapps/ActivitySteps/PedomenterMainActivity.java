package com.pedometer.asamapps.ActivitySteps;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pedometer.asamapps.Activities.BMI_Calculator;
import com.pedometer.asamapps.Activities.MyLocation;
import com.pedometer.asamapps.Activities.Settings;
import com.pedometer.asamapps.Fragments.NavigationDrawerFragment;
import com.pedometer.asamapps.R;
import com.pedometer.asamapps.Services.Time;
import com.pedometer.asamapps.UtilsAndOthers.PedometerUtils;
import com.pedometer.asamapps.compass.Compass;
import com.pedometer.asamapps.preferences.PedometerSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class PedomenterMainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener, SensorEventListener {

    private static final String TAG = "Pedometer";
    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    private static final int CALORIES_MSG = 5;
    private static final int TIME_MSG = 6;
    TextView mDesiredPaceView;
    long timeInMillies = 0L;
    long timeSwap = 0L;
    long finalTime = 0L;
    private Button p1_button;
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private Time mTimer;
    private PedometerUtils mPedometerUtils;
    private TextView RebootSteps;
    private TextView date1;
    private TextView direction;
    private TextView textView5;
    private TextView altitude;
    private TextView timer;
    private TextView mStepValueView;
    private TextView mPaceValueView;
    private TextView mDistanceValueView;
    private TextView mSpeedValueView;
    private TextView mCaloriesValueView;
    private TextView RS;
    private int mStepValue;
    private long mTimeValue;
    private TextView mTimeValueView;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private int mPaceValue;
    private float mDistanceValue;
    private float mSpeedValue;
    private int mCaloriesValue;
    private float mDesiredPaceOrSpeed;
    private int mMaintain;
    private Location location;
    private boolean mIsMetric;
    private float mMaintainInc;
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    private long startTime = 0L;
    private Handler myHandler = new Handler();
    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    private StepService mService;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int) msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) {
                        mPaceValueView.setText("0");
                    } else {
                        mPaceValueView.setText("" + (int) mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ((int) msg.arg1) / 1000f;
                    if (mDistanceValue <= 0) {
                        mDistanceValueView.setText("0");
                    } else {
                        mDistanceValueView.setText(
                                ("" + (mDistanceValue + 0.000001f)).substring(0, 5)
                        );
                    }
                    break;
                case SPEED_MSG:
                    mSpeedValue = ((int) msg.arg1) / 1000f;
                    if (mSpeedValue <= 0) {
                        mSpeedValueView.setText("0");
                    } else {
                        mSpeedValueView.setText(
                                ("" + (mSpeedValue + 0.000001f)).substring(0, 4)
                        );
                    }
                    break;
                case CALORIES_MSG:
                    mCaloriesValue = msg.arg1;
                    if (mCaloriesValue <= 0) {
                        mCaloriesValueView.setText("0");
                    } else {
                        mCaloriesValueView.setText("" + (int) mCaloriesValue);
                    }
                    break;
                case PedomenterMainActivity.TIME_MSG /*6*/:
                    PedomenterMainActivity.this.mTimeValue = ((Long) msg.obj).longValue();
                    PedomenterMainActivity.this.mTimeValueView.setText(PedometerUtils.formatTime(PedomenterMainActivity.this.mTimeValue + PedomenterMainActivity.this.getSharedPreferences("state", 0).getLong("time", 0)));
                default:
                    super.handleMessage(msg);
            }
        }

    };
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }

        public void paceChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }

        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int) (value * 1000), 0));
        }

        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int) (value * 1000), 0));
        }

        public void caloriesChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG, (int) (value), 0));
        }

        @Override
        public void timeChanged(long value) {
            PedomenterMainActivity.this.mHandler.sendMessage(PedomenterMainActivity.this.mHandler.obtainMessage(PedomenterMainActivity.TIME_MSG, Long.valueOf(value)));
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder) service).getService();


            mService.registerCallback(mCallback);
            mService.reloadSettings();

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };

    /**
     * Called when the activity is first created.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getFragmentManager();
        // GlobalFeedFragment feedFragment = new GlobalFeedFragment();
        // fragmentManager.beginTransaction().replace(R.id.container, feedFragment).commit();

        mStepValue = 0;
        mPaceValue = 0;

        startService(new Intent(this, Time.class));
        Log.i(TAG, "Started service");

        try {
            if (!mIsRunning) {
                p1_button.setText("Start ►");
                //p1_button.setBackgroundResource(R.drawable.resume);
            } else if (mIsRunning) {
                p1_button.setText("Stop ◼");
                //p1_button.setBackgroundResource(R.drawable.pause);

            }
        } catch (NullPointerException e) {

        }

        mPedometerUtils = PedometerUtils.getInstance();

        RebootSteps = (TextView) findViewById(R.id.time);
        RS = (TextView)findViewById(R.id.textView8);

        PackageManager m = getPackageManager();
        if (!m.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            RS.setVisibility(View.INVISIBLE);
            RebootSteps.setVisibility(View.INVISIBLE);
        }

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(br, new IntentFilter(Time.COUNTDOWN_BR));
        Log.i(TAG, "Registered broadcast receiver");
        mSensorManager.registerListener(this, mStepCounterSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST);

        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);

        mPedometerUtils.setSpeak(mSettings.getBoolean("speak", false));

        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();

        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startStepService();
            bindStepService();
            Button p1_button = (Button) findViewById(R.id.button2);
            p1_button.setText("Start ►");
            //p1_button.setBackgroundResource(R.drawable.resume);
        } else if (mIsRunning) {
            bindStepService();
            Button p1_button = (Button) findViewById(R.id.button2);
            p1_button.setText("Stop ◼");
            //p1_button.setBackgroundResource(R.drawable.pause);
        }

        mPedometerSettings.clearServiceRunning();

        mStepValueView = (TextView) findViewById(R.id.step_value);
        mPaceValueView = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
        mSpeedValueView = (TextView) findViewById(R.id.speed_value);
        mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
        mDesiredPaceView = (TextView) findViewById(R.id.desired_pace_value);
        mTimeValueView = (TextView) findViewById(R.id.textView);


        mIsMetric = mPedometerSettings.isMetric();
        ((TextView) findViewById(R.id.distance_units)).setText(getString(
                mIsMetric
                        ? R.string.kilometers
                        : R.string.miles
        ));
        ((TextView) findViewById(R.id.speed_units)).setText(getString(
                mIsMetric
                        ? R.string.kilometers_per_hour
                        : R.string.miles_per_hour
        ));

        mMaintain = mPedometerSettings.getMaintainOption();
        ((LinearLayout) this.findViewById(R.id.desired_pace_control)).setVisibility(
                mMaintain != PedometerSettings.M_NONE
                        ? View.VISIBLE
                        : View.INVISIBLE
        );
        if (mMaintain == PedometerSettings.M_PACE) {
            mMaintainInc = 5f;
            mDesiredPaceOrSpeed = (float) mPedometerSettings.getDesiredPace();
        } else if (mMaintain == PedometerSettings.M_SPEED) {
            mDesiredPaceOrSpeed = mPedometerSettings.getDesiredSpeed();
            mMaintainInc = 0.1f;
        }

        Button b1 = (Button) findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PedomenterMainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        Button b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mIsRunning) {

                    startStepService();
                    bindStepService();
                    Button p1_button = (Button) findViewById(R.id.button2);
                    p1_button.setText("Stop ◼");
                    //p1_button.setBackgroundResource(R.drawable.resume);
                } else {

                    unbindStepService();
                    stopStepService();
                    Button p1_button = (Button) findViewById(R.id.button2);
                    p1_button.setText("Start ►");
                    //p1_button.setBackgroundResource(R.drawable.pause);
                }
            }
        });

        Button b3 = (Button) findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetValues(true);
                mTimeValueView.setText("00:00");
                getSharedPreferences("state", 0).edit().putLong("time", 0).commit();
            }
        });
        Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed -= mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed += mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        if (mMaintain != PedometerSettings.M_NONE) {
            ((TextView) findViewById(R.id.desired_pace_label)).setText(
                    mMaintain == PedometerSettings.M_PACE
                            ? R.string.desired_pace
                            : R.string.desired_speed
            );
        }
        displayDesiredPaceOrSpeed();
    }

    private void displayDesiredPaceOrSpeed() {
        if (mMaintain == PedometerSettings.M_PACE) {
            mDesiredPaceView.setText("" + (int) mDesiredPaceOrSpeed);
        } else {
            mDesiredPaceView.setText("" + mDesiredPaceOrSpeed);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        if (mIsRunning) {
            unbindStepService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        } else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }

        super.onPause();
        //unregisterReceiver(br);
        Log.i(TAG, "Unregistered broacast receiver");
        savePaceSetting();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);

        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        Log.i(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    protected void onDestroy() {
        stopService(new Intent(this, Time.class));
        Log.i(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
        }
    }

    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onRestart();
        startActivity(getIntent());
    }

    private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
        if (mService != null) {
            if (mMaintain == PedometerSettings.M_PACE) {
                mService.setDesiredPace((int) desiredPaceOrSpeed);
            } else if (mMaintain == PedometerSettings.M_SPEED) {
                mService.setDesiredSpeed(desiredPaceOrSpeed);
            }
        }
    }

    private void savePaceSetting() {
        mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
    }

    private void startStepService() {
        if (!mIsRunning) {
            Log.i(TAG, "[SERVICE] Start");
            mIsRunning = true;
            startService(new Intent(PedomenterMainActivity.this,
                    StepService.class));
        }
    }

    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        bindService(new Intent(PedomenterMainActivity.this,
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {
        Log.i(TAG, "[SERVICE] Unbind");
        unbindService(mConnection);
    }

    private void stopStepService() {
        Log.i(TAG, "[SERVICE] Stop");
        if (mService != null) {
            Log.i(TAG, "[SERVICE] stopService");
            stopService(new Intent(PedomenterMainActivity.this,
                    StepService.class));
        }
        mIsRunning = false;
    }

    private void resetValues(boolean updateDisplay) {
        if (mService != null && mIsRunning) {
            mService.resetValues();

        } else {
            TextView timer = (TextView) findViewById(R.id.textView);
            //timer.setText("00:00");
            finalTime = 0;
            timeSwap = 0;
            timeInMillies = 0;
            mStepValueView.setText("0");
            mPaceValueView.setText("0");
            mDistanceValueView.setText("0");
            mSpeedValueView.setText("0");
            mCaloriesValueView.setText("0");
            mTimeValueView.setText("00:00");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.putInt("pace", 0);
                stateEditor.putFloat("distance", 0);
                stateEditor.putFloat("speed", 0);
                stateEditor.putFloat("calories", 0);
                stateEditor.putLong("time", 0);
                stateEditor.commit();
            }
        }
        Toast.makeText(getApplicationContext(),
                "Pedometer Reset!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {

    }

    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            RebootSteps.setText(" " + value);
        } /*else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // For test only.
            textView.setText("Step Detector Detected : " + value);
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(this, BMI_Calculator.class);
                break;
            case 1:
                intent = new Intent(this, Compass.class);
                break;
            case 2:
                intent = new Intent(this, MyLocation.class);
                break;
            case 3:
                intent = new Intent(this, Settings.class);
                break;
        }
        startActivity(intent);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.share_menu, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.Share:
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c.getTime());
                String text1 = formattedDate;
                String text2 = ((TextView)
                        findViewById(R.id.step_value)).getText().toString();
                String text5 = ((TextView)
                        findViewById(R.id.calories_value)).getText().toString();
                String text6 = ((TextView)
                        findViewById(R.id.pace_value)).getText().toString();
                String[] text7 = ((TextView)
                        findViewById(R.id.textView)).getText().toString().split(":");
                String text7a;
                if (text7.length == 3) {
                    text7a = text7[0] + " hours, " + text7[1] + " minutes, " + " and " + text7[2] + " seconds.";
                } else if (text7.length == 2) {
                    text7a = text7[0] + " minutes " + "and " + text7[1] + " seconds.";
                } else if (text7.length == 1) {
                    text7a = text7[0] + " seconds.";
                } else {
                    text7a = "Time Unavailable.";
                }
                String text8 = ((TextView)
                        findViewById(R.id.distance_value)).getText().toString();
                String text89 = ((TextView)
                        findViewById(R.id.distance_units)).getText().toString();
                String text9 = ((TextView)
                        findViewById(R.id.speed_value)).getText().toString();
                String text99 = ((TextView)
                        findViewById(R.id.speed_units)).getText().toString();
                String text3 = "On " + text1 + " I walked " + text2 + " steps. I burned " +
                        text5 + " calories. My pace was " + text6 + " steps/minute. My distance was " + text8 + " " + text89 +
                        ". My average speed was " + text9 + " " + text99 + ". This took me a total of " +
                        text7a;
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = text3;
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}