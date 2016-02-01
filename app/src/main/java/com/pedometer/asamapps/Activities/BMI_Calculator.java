package com.pedometer.asamapps.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.R;

public class BMI_Calculator extends AppCompatActivity implements View.OnClickListener{

    private static final int MENU_SETTINGS = 8;
    private PedometerSettings mPedometerSettings;
    private boolean mIsMetric;
    private SharedPreferences mSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi__calculator);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("BMI Calculator");

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);

        mIsMetric = mPedometerSettings.isMetric();
        ((EditText) findViewById(R.id.editText2)).setVisibility(
                mIsMetric
                        ? View.GONE
                        : View.VISIBLE
        );

        mIsMetric = mPedometerSettings.isMetric();
        ((EditText) findViewById(R.id.editText2)).setVisibility(
                mIsMetric
                        ? View.GONE
                        : View.VISIBLE
        );

        mIsMetric = mPedometerSettings.isMetric();
        EditText e1 = (EditText) findViewById(R.id.editText);
        if (mIsMetric) {
            e1.setHint("Centimeters");
        } else {
            e1.setHint("Feet");
        }

        mIsMetric = mPedometerSettings.isMetric();
        EditText e3 = (EditText) findViewById(R.id.editText3);
        if (mIsMetric) {
            e3.setHint("Kilograms");
        } else {
            e3.setHint("Pounds");
        }

        Button b = (Button) findViewById(R.id.button4);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                TextView t17 = (TextView) findViewById(R.id.textView17);
                TextView t18 = (TextView) findViewById(R.id.textView18);
                TextView t19 = (TextView) findViewById(R.id.textView19);
                TextView t20 = (TextView) findViewById(R.id.textView20);
                if (mIsMetric) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        EditText e1 = (EditText) findViewById(R.id.editText);
                        String c = e1.getText().toString();
                        Double cm = Double.parseDouble(c);
                        Double m = cm * 0.01;
                        EditText e3 = (EditText) findViewById(R.id.editText3);
                        String k = e3.getText().toString();
                        Double kg = Double.parseDouble(k);
                        Double mBMI = (kg / (m * m));
                        mBMI = (double) Math.round(mBMI * 10);
                        mBMI = mBMI / 10;
                        TextView t1 = (TextView) findViewById(R.id.textView22);
                        t1.setText("Your BMI is " + mBMI.toString());
                        if (mBMI < 18.5) {
                            t17.setTextColor(Color.BLUE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.WHITE);
                        } else if (mBMI >= 18.5 && mBMI <= 24.9) {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.GREEN);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.WHITE);
                        } else if (mBMI >= 25 && mBMI <= 29.9) {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.YELLOW);
                            t20.setTextColor(Color.WHITE);
                        } else {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.RED);
                        }
                    } catch (NumberFormatException e) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        Snackbar.make(findViewById(android.R.id.content), "Please fill in all the fields to get BMI result!", Snackbar.LENGTH_LONG).show();
                    }
                }

                else {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        EditText e1 = (EditText) findViewById(R.id.editText);
                        EditText e2 = (EditText) findViewById(R.id.editText2);
                        String f = e1.getText().toString();
                        Double feet = Double.parseDouble(f);
                        Double inches = feet * 12;
                        String i = e2.getText().toString();
                        Double in = Double.parseDouble(i);
                        Double tinches = inches + in;
                        EditText e3 = (EditText) findViewById(R.id.editText3);
                        String p = e3.getText().toString();
                        Double pounds = Double.parseDouble(p);
                        Double mBMI = (pounds / (tinches * tinches)) * 703;
                        mBMI = (double) Math.round(mBMI * 10);
                        mBMI = mBMI / 10;
                        TextView t1 = (TextView) findViewById(R.id.textView22);
                        t1.setText("Your BMI is " + mBMI.toString() + ".");
                        if (mBMI < 18.5) {
                            t17.setTextColor(Color.BLUE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.WHITE);
                        } else if (mBMI >= 18.5 && mBMI <= 24.9) {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.GREEN);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.WHITE);
                        } else if (mBMI >= 25 && mBMI <= 29.9) {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.YELLOW);
                            t20.setTextColor(Color.WHITE);
                        } else {
                            t17.setTextColor(Color.WHITE);
                            t18.setTextColor(Color.WHITE);
                            t19.setTextColor(Color.WHITE);
                            t20.setTextColor(Color.RED);
                        }
                    } catch(NumberFormatException e){
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        Snackbar.make(findViewById(android.R.id.content), "Please fill in all the fields to get BMI result!", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    //BMI = ( Weight in Pounds / ( Height in inches x Height in inches ) ) x 703
    //BMI = ( Weight in Kilograms / ( Height in Meters x Height in Meters ) )
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.bmi, menu);
        return true;

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;

        }
        return false;
    }

    @Override
    public void onClick(View view) {

    }
}
