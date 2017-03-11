package com.gilshelef.feedme.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gilshe on 2/25/17.
 */
public class Association {

    private static Association instance;
    private static String UUID;
    private static LatLng position;
    private static String name;
    private static final int KILOMETER = 1000;


    private Association(String id, LatLng location, String name){
        Association.UUID = id;
        Association.position = location;
        Association.name = name;
    }

    public static Association get(Activity activity) {
        if (instance == null) {
            synchronized (Association.class) {
                if (instance == null)
                    return build(activity);
            }
        }
        return instance;
    }

    private static Association build(Activity activity) {

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String uuid = sharedPref.getString("key_uuid", "1"); // TODO change to null after registering
        String name = sharedPref.getString("key_name", "לשובע"); // TODO change to null after registering
        float latitude = sharedPref.getFloat("key_latitude", 31.252973f);
        float longitude = sharedPref.getFloat("key_longitude", 34.791462f);

        LatLng base= new LatLng(latitude, longitude);
        instance = new Association(uuid, base, name);
        return instance;


    }

    public static float calcDistance(LatLng location) {
        float[] result = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, Association.position.latitude, Association.position.longitude, result);

        if(result.length == 0)
            return Integer.MAX_VALUE;
        return (result[0] / KILOMETER);
    }

    public static String getId() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public LatLng getBasePosition() {
        return position;
    }
}
