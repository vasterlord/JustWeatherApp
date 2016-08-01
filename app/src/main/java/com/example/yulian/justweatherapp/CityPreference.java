package com.example.yulian.justweatherapp;

/**
 * Created by Yulian on 30.07.2016.
 */
import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity(){
        return prefs.getString("city", "Lviv, UA");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}
