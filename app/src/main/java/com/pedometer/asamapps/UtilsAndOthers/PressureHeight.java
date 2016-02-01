package com.pedometer.asamapps.UtilsAndOthers;

/**
 * Created by Aravind on 11/21/2015.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import android.location.Location;

public class PressureHeight {

    // Air molar mass             = 0.0289644
    // Gravitational acceleration = 9.80665
    // Gas constant               = 8.31446217576

    private static final double ug_R = (-1 * 0.0289644 * 9.80665) / 8.31446217576;
    private static final double normal_pressure = 1013.25; // hPa;

    private double pressure_start; // Pa
    private double pressure_final; // Pa
    private double altitude;      // m
    private double temperature;   // K

    private void calculate_altitude(boolean use_normal_pressure)
    {
        if (this.temperature == 0)
            return;
        if (use_normal_pressure)
            this.altitude = java.lang.Math.log(this.pressure_final / normal_pressure) / (ug_R / this.temperature);
        else
            this.altitude = java.lang.Math.log(this.pressure_final / this.pressure_start) / (ug_R / this.temperature);
    }

    public void set_pressure_start(double value)
    {
        this.pressure_start = value;
    }

    public double get_pressure_start()
    {
        return this.pressure_start;
    }

    public void set_pressure_final(double value)
    {
        this.pressure_final = value;
    }

    public void set_temperature_kelvin(double value)
    {
        this.temperature = value;
        calculate_altitude(false);
    }

    public void set_temperature_celcius(double value)
    {
        this.temperature = value + 273.15;
        calculate_altitude(false);
    }

    public double get_altitude(boolean use_normal_pressure)
    {
        calculate_altitude(use_normal_pressure);
        return this.altitude;
    }
}
