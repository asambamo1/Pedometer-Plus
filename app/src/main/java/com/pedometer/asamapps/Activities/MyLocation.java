package com.pedometer.asamapps.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pedometer.asamapps.compass.Compass;
import com.pedometer.asamapps.preferences.PedometerSettings;
import com.pedometer.asamapps.R;
import com.pedometer.asamapps.Services.AppLocationService;
import com.pedometer.asamapps.UtilsAndOthers.LocationAddress;
import com.pedometer.asamapps.weather.JSONWeatherParser;
import com.pedometer.asamapps.weather.Weather;
import com.pedometer.asamapps.weather.WeatherHttpClient;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Aravind on 12/3/2015.
 */
public class MyLocation extends AppCompatActivity {

    Button refresh;
    AppLocationService appLocationService;
    TextView mylocation;
    private AsyncTask loader;
    private TextView cityText;
    private PedometerSettings mPedometerSettings;
    private TextView condDescr;
    private TextView temp;
    private TextView press;
    private boolean mIsMetric;
    private TextView windSpeed;
    private TextView windDeg;
    private TextView hum;
    private ImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Location");

        appLocationService = new AppLocationService(MyLocation.this);
        TextView mylocation = (TextView) findViewById(R.id.location);
        cityText = (TextView) findViewById(R.id.cityText);
        condDescr = (TextView) findViewById(R.id.condDescr);
        temp = (TextView) findViewById(R.id.temp);
        hum = (TextView) findViewById(R.id.hum);
        press = (TextView) findViewById(R.id.press);
        windSpeed = (TextView) findViewById(R.id.windSpeed);
        windDeg = (TextView) findViewById(R.id.windDeg);
        imgView = (ImageView) findViewById(R.id.condIcon);


        Location gpsLocation = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER);
        Location location = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();
            String uri = "www.google.com/maps?q=";
            String comma = ",";
            String result = "My Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\n" + uri + latitude + comma
                    + longitude;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String cityName = "";
            try {
                cityName = addresses.get(0).getLocality();
            } catch (Exception e){

            }
            String city = cityName;
            JSONWeatherTask task = new JSONWeatherTask();
            task.execute(new String[]{city});
        } else {
            gpsLocation = appLocationService
                    .getLocation(LocationManager.NETWORK_PROVIDER);
            location = appLocationService
                    .getLocation(LocationManager.NETWORK_PROVIDER);
            if (gpsLocation != null) {
                double latitude = gpsLocation.getLatitude();
                double longitude = gpsLocation.getLongitude();
                String uri = "www.google.com/maps?q=";
                String comma = ",";
                String result = "My Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\n" + uri + latitude + comma
                        + longitude;
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cityName = addresses.get(0).getLocality();
                String city = cityName;
                JSONWeatherTask task = new JSONWeatherTask();
                task.execute(new String[]{city});
            } else {
                showSettingsAlert();
            }

        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        } else {
            mylocation.setText("Location Unavailable.");
            cityText.setText(" Weather Information Unavailable");

        }


        refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Location gpsLocation = appLocationService
                        .getLocation(LocationManager.GPS_PROVIDER);
                Location location = appLocationService
                        .getLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    double latitude = gpsLocation.getLatitude();
                    double longitude = gpsLocation.getLongitude();
                    String uri = "www.google.com/maps?q=";
                    String comma = ",";
                    String result = "My Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\n" + uri + latitude + comma
                            + longitude;
                    Geocoder geocoder = new Geocoder(MyLocation.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String cityName = "";
                    try {
                        cityName = addresses.get(0).getLocality();
                    }
                    catch(Exception e) {

                    }
                    String city = cityName;
                    JSONWeatherTask task = new JSONWeatherTask();
                    task.execute(new String[]{city});
                } else {
                    gpsLocation = appLocationService
                            .getLocation(LocationManager.NETWORK_PROVIDER);
                    location = appLocationService
                            .getLocation(LocationManager.NETWORK_PROVIDER);
                    if (gpsLocation != null) {
                        double latitude = gpsLocation.getLatitude();
                        double longitude = gpsLocation.getLongitude();
                        String uri = "www.google.com/maps?q=";
                        String comma = ",";
                        String result = "My Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\n" + uri + latitude + comma
                                + longitude;
                        Geocoder geocoder = new Geocoder(MyLocation.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String cityName = addresses.get(0).getLocality();
                        String city = cityName;
                        JSONWeatherTask task = new JSONWeatherTask();
                        task.execute(new String[]{city});
                    } else {
                        showSettingsAlert();
                    }

                }

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LocationAddress locationAddress = new LocationAddress();
                    locationAddress.getAddressFromLocation(latitude, longitude,
                            getApplicationContext(), new GeocoderHandler());
                } else {
                    TextView mylocation = (TextView) findViewById(R.id.location);
                    mylocation.setText("Location Unavailable.");
                    cityText.setText(" Weather Information Unavailable");

                }


            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        // Cancel the task
        if(loader != null && loader.getStatus() != AsyncTask.Status.FINISHED) {
            loader.cancel(true);
        }

        //loader.cancel(true);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MyLocation.this);
        alertDialog.setTitle("Enable GPS?");
        alertDialog.setMessage("Location Provider Disabled. Go to Settings Menu to enable it?");
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MyLocation.this.startActivity(intent);

                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.location, menu);
        return true;

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;

            case R.id.share:
                String text2 = ((TextView)
                        findViewById(R.id.location)).getText().toString();
                String text3 = text2;
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = text3;
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;

            case R.id.maps:

                Location gpsLocation = appLocationService
                        .getLocation(LocationManager.NETWORK_PROVIDER);
                Location location = appLocationService
                        .getLocation(LocationManager.NETWORK_PROVIDER);
                if (gpsLocation != null) {
                    double latitude = gpsLocation.getLatitude();
                    double longitude = gpsLocation.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<" + latitude + ">,<" + longitude + ">?q=<" + latitude + ">,<" + longitude));
                    startActivity(intent);
                } else {

                    showSettingsAlert();

                }
                return true;

            case R.id.Compass:
                Intent Intent = new Intent(this, Compass.class);
                startActivity(Intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            TextView mylocation = (TextView) findViewById(R.id.location);
            mylocation.setText(locationAddress);
        }
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = JSONWeatherParser.getWeather(data);

                // Let's retrieve the icon
                weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
            return weather;

        }

        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }

            try {
                cityText.setText(" City: " + weather.location.getCity() + ", " + weather.location.getCountry());
                condDescr.setText(" Weather: " + weather.currentCondition.getCondition() + " (" + weather.currentCondition.getDescr() + ")");
                long temp1 = Math.round((weather.temperature.getTemp() - 273.15));
                temp.setText(" Temperature: " + Math.round((weather.temperature.getTemp() - 273.15)) + "°C/" + Math.round(temp1 * (1.8) + 32) + "°F");
                hum.setText(" Humidity: " + weather.currentCondition.getHumidity() + "%");
                press.setText(" Pressure: " + weather.currentCondition.getPressure() + " hPa");
                windSpeed.setText(" Wind: " + weather.wind.getSpeed() + " mps");
                long wind1 = (Math.round(weather.wind.getSpeed() * 36) / 10);
                double wind2 = (wind1 * 0.621371);
                long wind3 = (Math.round(wind2));
                windDeg.setText(" Wind: " + wind1 + " kph/" + wind3 + " mph");
            } catch (NullPointerException e) {
                cityText.setText(" Weather Information Currently Unavailable");
            }

        }


    }

}

