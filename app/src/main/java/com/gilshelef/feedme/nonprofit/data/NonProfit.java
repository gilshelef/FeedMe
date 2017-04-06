package com.gilshelef.feedme.nonprofit.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import com.gilshelef.feedme.launcher.RegistrationActivity;

/**
 * Created by gilshe on 2/25/17.
 */
public class NonProfit {

    private static final int KILOMETER = 1000;
    public static final String KEY_NAME = "key_name";
    public static final String KEY_CONTACT = "key_contact_name";
    public static final String KEY_PHONE = "key_contact_phone";
    public static final String KEY_LAT = "key_latitude";
    public static final String KEY_LNG = "key_longitude";
    public static final String KEY_UUID = "key_uuid";
    public static final String KEY_ADDRESS = "key_address";

    private static NonProfit instance;
    private String contactPhone;
    private String UUID;
    private LatLng basePosition;
    private String nonProfitName;
    private String contactName;
    private String nonProfitAddress;


    private NonProfit(String uuid, LatLng basePosition, String nonProfitName, String contactName, String contactPhone, String nonProfitAddress){
        this.UUID = uuid;
        this.basePosition = basePosition;
        this.nonProfitName = nonProfitName;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.nonProfitAddress = nonProfitAddress;
    }

    public static NonProfit get(Activity activity) {
        if (instance == null) {
            synchronized (NonProfit.class) {
                if (instance == null)
                    return build(activity);
            }
        }
        return instance;
    }

    private static NonProfit build(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        String uuid = sharedPref.getString(KEY_UUID, "0");
        String nonProfitName = sharedPref.getString(KEY_NAME, "עמותה"); // TODO change to null after registering
        String nonProfitAddress = sharedPref.getString(KEY_ADDRESS, "");
        float latitude = sharedPref.getFloat(KEY_LAT, 31.252973f);
        float longitude = sharedPref.getFloat(KEY_LNG, 34.791462f);
        String contactName = sharedPref.getString(KEY_CONTACT, "איש קשר");
        String contactPhone = sharedPref.getString(KEY_PHONE, "");
        LatLng basePosition = new LatLng(latitude, longitude);
        instance = new NonProfit(uuid, basePosition, nonProfitName, contactName, contactPhone, nonProfitAddress);
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

    public String getAddress() {
        return nonProfitAddress;
    }

    public void setAddress(Context context, LatLng latLng, String address) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_ADDRESS, address);
        editor.putFloat(NonProfit.KEY_LAT, (float) latLng.latitude);
        editor.putFloat(NonProfit.KEY_LNG, (float) latLng.longitude);
        editor.apply();

        this.nonProfitAddress = address;
        this.basePosition = latLng;
    }

    public void setContact(Context context, String contactName) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_CONTACT, contactName);
        editor.apply();
        this.contactName = contactName;
    }

    public void setPhone(Context context, String contactPhone) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_PHONE, contactPhone);
        editor.apply();
        this.contactPhone = contactPhone;
    }

    public void setNonProfitName(Context context, String nonProfitName, String uuid) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_UUID, uuid);
        editor.putString(NonProfit.KEY_NAME, nonProfitName);
        editor.apply();

        this.nonProfitName = nonProfitName;
        this.UUID = uuid;
    }

    private SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    public void clear() {
        instance = null;
    }
}
