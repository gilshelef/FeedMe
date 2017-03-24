package com.gilshelef.feedme.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import launcher.RegistrationActivity;

/**
 * Created by gilshe on 2/25/17.
 */
public class Association{

    private static final int KILOMETER = 1000;
    public static final String KEY_NAME = "key_name";
    public static final String KEY_CONTACT = "key_contact_name";
    public static final String KEY_PHONE = "key_contact_phone";
    public static final String KEY_LAT = "key_latitude";
    public static final String KEY_LNG = "key_longitude";
    public static final String KEY_UUID = "key_uuid";
    private static Association instance;

    private String contactPhone;
    private String UUID;
    private LatLng basePosition;
    private String nonProfitName;
    private String contactName;


    private Association(String uuid, LatLng basePosition, String nonProfitName, String contactName, String contactPhone){
        this.UUID = uuid;
        this.basePosition = basePosition;
        this.nonProfitName = nonProfitName;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
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
        SharedPreferences sharedPref = activity.getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        String uuid = sharedPref.getString(KEY_UUID, "0");
        String nonProfitName = sharedPref.getString(KEY_NAME, "עמותה"); // TODO change to null after registering
        float latitude = sharedPref.getFloat(KEY_LAT, 31.252973f);
        float longitude = sharedPref.getFloat(KEY_LNG, 34.791462f);
        String contactName = sharedPref.getString(KEY_CONTACT, "איש קשר");
        String contactPhone = sharedPref.getString(KEY_PHONE, "");
        LatLng basePosition = new LatLng(latitude, longitude);
        instance = new Association(uuid, basePosition, nonProfitName, contactName, contactPhone);
        return instance;
    }

    public float calcDistance(LatLng location) {
        float[] result = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, basePosition.latitude, basePosition.longitude, result);

        if(result.length == 0)
            return Integer.MAX_VALUE;
        return (result[0] / KILOMETER);
    }

    public String getId() {
        return UUID;
    }

    public String getName() {
        return nonProfitName;
    }

    public LatLng getBasePosition() {
        return basePosition;
    }

    public String getContact() {
        return contactName;
    }

    public String getPhone(){
        return contactPhone;
    }
}
