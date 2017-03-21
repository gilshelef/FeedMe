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
    private static final int KILOMETER = 1000;
    private String UUID;
    private LatLng position;
    private String name;
    private String email;


    private Association(String uuid, LatLng position, String name, String email){
        this.UUID = uuid;
        this.position = position;
        this.name = name;
        this.email = email;

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
        String email = sharedPref.getString("key_email", activity.getResources().getString(com.gilshelef.feedme.R.string.email_placeholder));
        LatLng base= new LatLng(latitude, longitude);
        instance = new Association(uuid, base, name, email);
        return instance;


    }

    public float calcDistance(LatLng location) {
        float[] result = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, position.latitude, position.longitude, result);

        if(result.length == 0)
            return Integer.MAX_VALUE;
        return (result[0] / KILOMETER);
    }

    public String getId() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public LatLng getBasePosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }
}
