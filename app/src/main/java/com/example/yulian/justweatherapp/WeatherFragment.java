package com.example.yulian.justweatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherFragment extends Fragment {
    Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;
    TextView descriptonField;
    FrameLayout focusFragment;
    Handler handler;
    String[] lastWeather = new String[6];
    String curWeather;
    String[] oldWeather;

    final String SAVED_TEXT = "saved_text";
    public WeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
        descriptonField = (TextView)rootView.findViewById(R.id.decription_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
        focusFragment = (FrameLayout)rootView.findViewById(R.id.focus_layout);

        weatherIcon.setTypeface(weatherFont);
        oldWeather = loadArray(curWeather,getContext());
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }
//////////////////////////////// Logic of app:
    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if((json == null) && (isOnline(getContext()))){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else if(!isOnline(getContext())){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.no_internet_connetion),
                                    Toast.LENGTH_LONG).show();
                            lastWeatherPaste();
                            Design();
                        }
                    });
                }
                else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                            Design();
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");
            JSONObject clouds = json.getJSONObject("clouds");
            descriptonField.setText(details.getString("description").toUpperCase(Locale.US));
            detailsField.setText( "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa" +
                            "\n" + "Wind speed: " + wind.getString("speed") + " meter/sec" +
                            "\n" + "Wind direction: " + String.format("%.0f", wind.getDouble("deg"))   + " degrees" +
                            "\n" + "Cloudness: " + clouds.getString("all") + " %"
            );

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);
            lastWeatherUp();
        }catch(Exception e){
            Log.e("JustWeather", "One or more fields not found in the JSON data");
        }
    }
    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;

        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }
    public void changeCity(String city){
        updateWeatherData(city);
    }
    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Dynamical theme of app:
    private void Design() {
        if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_sunny)){
            focusFragment.setBackgroundResource(R.color.sunny);
            ColorWhite("#fffafa");
            weatherIcon.setTextColor(Color.parseColor("#fffafa"));
        } else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_clear_night)){
            focusFragment.setBackgroundResource(R.color.clear_night);
            ColorWhite("#fffafa");
            weatherIcon.setTextColor(Color.parseColor("#fffafa"));
        } else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_thunder)){
            focusFragment.setBackgroundResource(R.color.thunder);
            ColorWhite("#fffafa");
        }else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_drizzle)){
            focusFragment.setBackgroundResource(R.color.drizzle);
            ColorWhite("#fffafa");
        } else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_foggy)){
            focusFragment.setBackgroundResource(R.color.foggy);
            ColorWhite("#fffafa");
        } else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_cloudy)){
            focusFragment.setBackgroundResource(R.color.cloudy);
            ColorBlack("#000000");
        } else if(weatherIcon.getText().toString() ==  getActivity().getString(R.string.weather_snowy)){
            focusFragment.setBackgroundResource(R.color.snowy);
            ColorBlack("#000000");
        } else if(weatherIcon.getText().toString() == null){
            focusFragment.setBackgroundResource(R.color.empty);
            ColorWhite("#fffafa");
        }
    }
    public void ColorBlack(String color) {
        cityField.setTextColor(Color.parseColor(color));
        descriptonField.setTextColor(Color.parseColor(color));
        updatedField.setTextColor(Color.parseColor(color));
        detailsField.setTextColor(Color.parseColor(color));
        currentTemperatureField.setTextColor(Color.parseColor(color));
        weatherIcon.setTextColor(Color.parseColor(color));
    }

    public void ColorWhite(String color) {
        cityField.setTextColor(Color.parseColor(color));
        descriptonField.setTextColor(Color.parseColor(color));
        updatedField.setTextColor(Color.parseColor(color));
        detailsField.setTextColor(Color.parseColor(color));
        currentTemperatureField.setTextColor(Color.parseColor(color));
        weatherIcon.setTextColor(Color.parseColor(color));
    }
    /////////////////////////////////////////////////////////////////////////////
    //////////////////// Work with data for shared preferences:
    private void lastWeatherPaste() {
        cityField.setText(oldWeather[0]);
        descriptonField.setText(oldWeather[1]);
        detailsField.setText(oldWeather[2]);
        currentTemperatureField.setText(oldWeather[3]);
        updatedField.setText(oldWeather[4]);
        weatherIcon.setText(oldWeather[5]);
    }

    private void lastWeatherUp() {
        lastWeather[0] = cityField.getText().toString();
        lastWeather[1] =  descriptonField.getText().toString();
        lastWeather[2] = detailsField.getText().toString();
        lastWeather[3] = currentTemperatureField.getText().toString();
        lastWeather[4] =  updatedField.getText().toString();
        lastWeather[5] =  weatherIcon.getText().toString();
    }
    public boolean saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public String[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }
    @Override
    public void onStop() {
        super.onStop();
        lastWeatherUp();
        saveArray(lastWeather, curWeather , getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        lastWeatherUp();
        saveArray(lastWeather, curWeather , getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastWeatherUp();
        saveArray(lastWeather, curWeather , getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lastWeatherUp();
        saveArray(lastWeather, curWeather , getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
      oldWeather = loadArray(curWeather,getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        oldWeather = loadArray(curWeather,getContext());
    }
///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////
}
