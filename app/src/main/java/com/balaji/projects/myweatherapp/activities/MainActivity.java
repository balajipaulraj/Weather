package com.balaji.projects.myweatherapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.balaji.projects.myweatherapp.Constants;
import com.balaji.projects.myweatherapp.R;
import com.balaji.projects.myweatherapp.adapters.LocationSpinnerAdapter;
import com.balaji.projects.myweatherapp.adapters.ViewPagerAdapter;
import com.balaji.projects.myweatherapp.adapters.WeatherRecyclerAdapter;
import com.balaji.projects.myweatherapp.fragments.RecyclerViewFragment;
import com.balaji.projects.myweatherapp.models.Weather;
import com.balaji.projects.myweatherapp.tasks.GenericRequestTask;
import com.balaji.projects.myweatherapp.tasks.ParseResult;
import com.balaji.projects.myweatherapp.tasks.TaskOutput;
import com.balaji.projects.myweatherapp.utils.UnitConvertor;
import com.balaji.projects.myweatherapp.widgets.AbstractWidgetProvider;
/**
 * Created by Balaji on 30-May-18.
 */
public class MainActivity extends AppCompatActivity implements LocationListener {
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;

    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;

    Typeface weatherFont;
    Weather todayWeather = new Weather();

    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    ViewPager viewPager;
    TabLayout tabLayout;
    String[] cities = {"Select a City", "chennai", "Mumbai", "Bangalore", "New Delhi"};
    View appView;
    String weathercity;
    double lat, lng;

    LocationManager locationManager;
    ProgressDialog progressDialog;

    boolean destroyed = false;

    private List<Weather> longTermWeather = new ArrayList<>();
    private List<Weather> longTermTodayWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather = new ArrayList<>();

    public String recentCity = "";
    Spinner citySpinner;
    private String selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        appView = findViewById(R.id.viewApp);

        progressDialog = new ProgressDialog(MainActivity.this);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);


        // Initialize textboxes
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        citySpinner = (Spinner) findViewById(R.id.spin_city_selection);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
//        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
//        todayIcon.setTypeface(weatherFont);

        // Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        destroyed = false;

        initMappings();

        // Preload data from cache
        preloadWeather();
        citySpinner.setAdapter(new LocationSpinnerAdapter(MainActivity.this, R.layout.custom_spinner_item,
                cities));
        citySpinner.setSelection(1);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = citySpinner.getItemAtPosition(position).toString();
                if(!progressDialog.isShowing()) {
                    progressDialog.setMessage(getApplicationContext().getString(R.string.downloading_data));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                }
                switch (position) {
                    case 1:
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putString("selectedcity", selectedCity);
                        editor.commit();
                        getWeather();
                        break;
                    case 2:
                        SharedPreferences.Editor editor1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor1.putString("selectedcity", selectedCity);
                        editor1.commit();
                        getWeather();
                        break;
                    case 3:
                        SharedPreferences.Editor editor2 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor2.putString("selectedcity", selectedCity);
                        editor2.commit();
                        getWeather();
                        break;
                    case 4:
                        SharedPreferences.Editor editor3 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor3.putString("selectedcity", selectedCity);
                        editor3.commit();
                        getWeather();
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTodayWeatherUI();
        updateLongTermWeatherUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        if (locationManager != null) {
            try {
                locationManager.removeUpdates(MainActivity.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }


    private void preloadWeather() {
        if(!progressDialog.isShowing()) {
            progressDialog.setMessage(this.getString(R.string.downloading_data));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastLongterm);
        }
    }

    private void getTodayWeather() {
        new TodayWeatherTask(this, this, progressDialog).execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask(this, this, progressDialog).execute();
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = this.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    private ParseResult parseTodayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);

            JSONObject coordinates = reader.getJSONObject("coord");
            if (coordinates != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putFloat("latitude", (float) coordinates.getDouble("lon")).putFloat("longitude", (float) coordinates.getDouble("lat")).commit();
            }

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                todayWeather.setWindDirectionDegree(null);
            }
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            todayWeather.setRain(rain);

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateTodayWeatherUI() {
        try {
            if (todayWeather.getCountry().isEmpty()) {
                preloadWeather();
                return;
            }
        } catch (Exception e) {
            preloadWeather();
            return;
        }
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(todayWeather.getTemperature()), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = UnitConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeather.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConvertor.convertWind(wind, sp);

        // Pressure
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);

        todayTemperature.setText(new DecimalFormat("0.#").format(temperature) + " " + sp.getString("unit", "°C"));
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);
        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            todayWind.setText(getString(R.string.wind) + ": " +
                    UnitConvertor.getBeaufortName((int) wind) +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        } else {
            todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("0.0").format(wind) + " " +
                    localize(sp, "speedUnit", "m/s") +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        }
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("0.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
//        todayIcon.setText(todayWeather.getIcon());
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (longTermWeather == null) {
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    longTermTomorrowWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }

            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));

                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
                    longTermTomorrowWeather.add(weather);
                } else {
                    longTermWeather.add(weather);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateLongTermWeatherUI() {
        if (destroyed) {
            return;
        }
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    void getCityByLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explanation not needed, since user requests this themmself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        locationManager.removeUpdates(MainActivity.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCityByLocation();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    class TodayWeatherTask extends GenericRequestTask {
        public TodayWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            super.onPostExecute(output);
            // Update widgets
           // AbstractWidgetProvider.updateWidgets(MainActivity.this);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI() {
            updateTodayWeatherUI();
//            updateLastUpdateTime();
        }
    }

    class LongTermWeatherTask extends GenericRequestTask {
        public LongTermWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseLongTermJson(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI() {
            updateLongTermWeatherUI();
        }
    }


    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }


    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }


    private void getWeather() {
        Log.d("Alarm", "Recurring alarm; requesting download service.");
        boolean failed;
        if (isNetworkAvailable()) {
            failed = false;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String city = sp.getString("selectedcity", "chennai");
            findLocation(city);
            new GetCityNameTask().execute(String.valueOf(lat), String.valueOf(lng)); // This method calls the two methods below once it has determined a location
        } else {
            failed = true;
        }
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putBoolean("backgroundRefreshFailed", failed);
        editor.apply();
    }


    private boolean findLocation(String selectedCity) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> address;
        try {

            address = geocoder.getFromLocationName(selectedCity, 5);
            Address location = address.get(0);
            weathercity = location.getAddressLine(0);
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.e("LatLng", lat + "," + lng);
            getSupportActionBar().setTitle(weathercity);
            return true;

        } catch (Exception e) {
            Log.d("Error", "MY_ERROR : ############Address Not Found");
            return false;
        }
    }


//    private boolean isUpdateLocation() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        return preferences.getBoolean("updateLocationAutomatically", false);
//    }

    public class GetWeatherTask extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String result = "";
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                String apiKey = sp.getString("apiKey", MainActivity.this.getResources().getString(R.string.apiKey));
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("lastToday", result);
                    editor.apply();
                    MainActivity.saveLastUpdateTime(sp);
                } else {
                    // Connection problem
                }
            } catch (IOException e) {
                // No connection
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            // Update widgets
            //AbstractWidgetProvider.updateWidgets(MainActivity.this);
        }
    }

    class GetLongTermWeatherTask extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String result = "";
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if (language.equals("cs")) {
                    language = "cz";
                }
                String apiKey = sp.getString("apiKey", MainActivity.this.getResources().getString(R.string.apiKey));
                URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&mode=json&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("lastLongterm", result);
                    editor.apply();
                } else {
                    // Connection problem
                }
            } catch (IOException e) {
                // No connection
            }
            return null;
        }

        protected void onPostExecute(Void v) {
                getTodayWeather();
                getLongTermWeather();
        }
    }


    public class GetCityNameTask extends AsyncTask<String, String, Void> {
        private static final String TAG = "GetCityNameTask";

        @Override
        protected Void doInBackground(String... params) {
            String lat = params[0];
            String lon = params[1];

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String language = Locale.getDefault().getLanguage();
            if (language.equals("cs")) {
                language = "cz";
            }
            String apiKey = sp.getString("apiKey", MainActivity.this.getResources().getString(R.string.apiKey));

            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=&lat=" + lat + "&lon=" + lon + "&lang=" + language + "&appid=" + apiKey);
                Log.d(TAG, "Request: " + url.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String result = "";
                    String line;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    Log.d(TAG, "JSON Result: " + result);
                    try {
                        JSONObject reader = new JSONObject(result);
                        String city = reader.getString("name");
                        String country = "";
                        JSONObject countryObj = reader.optJSONObject("sys");
                        if (countryObj != null) {
                            country = ", " + countryObj.getString("country");
                        }
                        Log.d(TAG, "City: " + city + country);
                        String lastCity = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("city", "");
                        String currentCity = city + country;
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("city", currentCity);
                        editor.putBoolean("cityChanged", !currentCity.equals(lastCity));
                        editor.commit();

                    } catch (JSONException e) {
                        Log.e(TAG, "An error occurred while reading the JSON object", e);
                    }
                } else {
                    Log.e(TAG, "Error: Response code " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(TAG, "Connection error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new GetWeatherTask().execute();
            new GetLongTermWeatherTask().execute();
        }
    }

}
