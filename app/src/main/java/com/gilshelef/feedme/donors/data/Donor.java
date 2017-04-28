package com.gilshelef.feedme.donors.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.StringTokenizer;

/**
 * Created by gilshe on 3/26/17.
 */

public class Donor {

    public static final String KEY_BUS_NAME = "key_name";
    public static final String KEY_FIRST_NAME = "key_fname";
    public static final String KEY_LAST_NAME = "key_lname";
    public static final String KEY_PHONE = "key_contact_phone";
    public static final String KEY_LAT = "key_latitude";
    public static final String KEY_LNG = "key_longitude";
    public static final String KEY_UUID = "key_uuid";
    public static final String KEY_ADDRESS = "key_address";
    public static final String KAY_TYPE = "key_type";
    public static final String KEY_DONATION_COUNT = "key_donation_count";

    private static Donor instance;
    private String UUID;
    private LatLng position;
    private String businessName;
    private String address;
    private String phone;
    private Type donationType;
    private String firstName;
    private String lastName;
    private int donationCount;


    public Donor(String uuid, String businessName, String address, String contactFName, String contactLName, String phone, LatLng basePosition, Type donationType, int donationCount){
        this.UUID = uuid;
        this.businessName = businessName;
        this.address = address;
        this.firstName = contactFName;
        this.lastName = contactLName;
        this.phone = phone;
        this.position = basePosition;
        this.donationType = donationType;
        this.donationCount = donationCount;
    }

    public static Donor get(Activity activity) {
        if (instance == null) {
            synchronized (Donor.class) {
                if (instance == null)
                    return build(activity);
            }
        }
        return instance;
    }

    private static Donor build(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        String uuid = sharedPref.getString(KEY_UUID, "0");
        String businessName = sharedPref.getString(KEY_BUS_NAME, "");
        String address = sharedPref.getString(KEY_ADDRESS, "");
        float latitude = sharedPref.getFloat(KEY_LAT, 31.252973f);
        float longitude = sharedPref.getFloat(KEY_LNG, 34.791462f);
        String contactFName = sharedPref.getString(KEY_FIRST_NAME, "");
        String contactLName = sharedPref.getString(KEY_LAST_NAME, "");
        String phone = sharedPref.getString(KEY_PHONE, "");
        Type donationType = TypeManager.get().getTypeFromString(sharedPref.getString(KAY_TYPE, TypeManager.OTHER_DONATION));
        LatLng position = new LatLng(latitude, longitude);
        int donationCount = sharedPref.getInt(KEY_DONATION_COUNT, 0);
        instance = new Donor(uuid, businessName, address, contactFName, contactLName, phone, position, donationType, donationCount);
        return instance;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getContact() {
        return firstName + " " + lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public void setAddress(Context context, LatLng latLng, String address) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_ADDRESS, address);
        editor.putFloat(Donor.KEY_LAT, (float) latLng.latitude);
        editor.putFloat(Donor.KEY_LNG, (float) latLng.longitude);
        editor.apply();

        this.address = address;
        this.position = latLng;
    }

    private SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    public void setTypeByString(Context context, String typeStr) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        prefs.edit().putString(Donor.KAY_TYPE, typeStr).apply();
        this.donationType  = TypeManager.get().getType(typeStr);
    }

    public void setBusinessName(Context context, String newBusinessName) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_BUS_NAME, newBusinessName);
        editor.apply();
        this.businessName = newBusinessName;
    }

    public void setContact(Context context, String newContact) {
        StringTokenizer tokenizer = new StringTokenizer(newContact, " ");
        firstName = tokenizer.nextToken();
        if(tokenizer.hasMoreTokens())
            this.lastName = tokenizer.nextToken();
        else lastName = "";

        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_FIRST_NAME, firstName);
        editor.putString(Donor.KEY_LAST_NAME, lastName);
        editor.apply();
    }

    public Type getDonationType() {
        return donationType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LatLng getPosition() {
        return position;
    }

    public void clear() {
        instance = null;
    }

    public String getId() {
        return UUID;
    }

    public void setPhone(Context context, String contactPhone) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_PHONE, contactPhone);
        editor.apply();
        this.phone = contactPhone;
    }

    public int getDonationCount() {
        return donationCount;
    }
}
