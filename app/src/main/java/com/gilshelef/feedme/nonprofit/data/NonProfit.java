package com.gilshelef.feedme.nonprofit.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.google.android.gms.maps.model.LatLng;

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
    public static final String KEY_ID = "key_id";
    public static final String KEY_ADDRESS = "key_address";

    private static NonProfit instance;
    private String contactPhone;
    private String id;
    private LatLng position;
    private String name;
    private String contactName;
    private String address;


    public NonProfit(String id, LatLng position, String name, String contactName, String contactPhone, String address){
        this.id = id;
        this.position = position;
        this.name = name;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.address = address;
    }

    public static NonProfit get(Context context) {
        if (instance == null) {
            synchronized (NonProfit.class) {
                if (instance == null)
                    return build(context);
            }
        }
        return instance;
    }

    private static NonProfit build(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        String uuid = sharedPref.getString(KEY_ID, "0");
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


    //getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getContact() {
        return contactName;
    }

    public String getPhone(){
        return contactPhone;
    }

    public String getAddress() {
        return address;
    }

    //setters
    public void setAddress(Context context, LatLng latLng, String address) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_ADDRESS, address);
        editor.putFloat(NonProfit.KEY_LAT, (float) latLng.latitude);
        editor.putFloat(NonProfit.KEY_LNG, (float) latLng.longitude);
        editor.apply();

        this.address = address;
        this.position = latLng;
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

    public void setName(Context context, String nonProfitName) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(NonProfit.KEY_NAME, nonProfitName);
        editor.apply();

        this.name = nonProfitName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float calcDistance(LatLng location) {
        float[] result = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, position.latitude, position.longitude, result);

        if(result.length == 0)
            return Integer.MAX_VALUE;
        return (result[0] / KILOMETER);
    }

    private SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    public static void clear() {
        instance = null;
    }

    public boolean isOwned(Donation donation) {
        return donation.getNonProfitId() != null && donation.getNonProfitId().equals(getId());
    }
}
