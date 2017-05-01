package com.gilshelef.feedme.donors.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.StringTokenizer;

/**
 * Created by gilshe on 3/26/17.
 */

public class Donor {

    public static final String KEY_BUIS_NAME = "key_name";
    public static final String KEY_FIRST_NAME = "key_fname";
    public static final String KEY_LAST_NAME = "key_lname";
    public static final String KEY_PHONE = "key_contact_phone";
    public static final String KEY_LAT = "key_latitude";
    public static final String KEY_LNG = "key_longitude";
    public static final String KEY_ID = "key_uuid";
    public static final String KEY_ADDRESS = "key_address";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_DONATION_COUNT = "key_donation_count";
    private static final String TAG = Donor.class.getSimpleName();

    private static Donor instance;
    private String id;
    private LatLng position;
    private String businessName;
    private String address;
    private String phone;
    private Type donationType;
    private String firstName;
    private String lastName;
    private int donationCount;


    public Donor(String id, String businessName, String address, String contactFName, String contactLName, String phone, LatLng position, Type donationType, int donationCount){
        this.id = id;
        this.businessName = businessName;
        this.address = address;
        this.firstName = contactFName;
        this.lastName = contactLName;
        this.phone = phone;
        this.position = position;
        this.donationType = donationType;
        this.donationCount = donationCount;
    }

    public static Donor get(){
        return instance;
    }

    public static Donor get(Activity activity) {
        Log.d("BUG", "get in Donor");
        if (instance == null) {
            synchronized (Donor.class) {
                if (instance == null)
                    return build(activity);
            }
        }
        return instance;
    }

    private static Donor build(Activity activity) {
        Log.d("BUG", "build in Donor");

        SharedPreferences sharedPref = activity.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);

        String id = sharedPref.getString(KEY_ID, "0");
        String businessName = sharedPref.getString(KEY_BUIS_NAME, "");
        String address = sharedPref.getString(KEY_ADDRESS, "");
        float latitude = sharedPref.getFloat(KEY_LAT, 0);
        float longitude = sharedPref.getFloat(KEY_LNG, 0);
        String contactFName = sharedPref.getString(KEY_FIRST_NAME, "");
        String contactLName = sharedPref.getString(KEY_LAST_NAME, "");
        String phone = sharedPref.getString(KEY_PHONE, "");
        Type donationType = TypeManager.get().getType(sharedPref.getString(KEY_TYPE, TypeManager.OTHER_DONATION));
        LatLng position = new LatLng(latitude, longitude);
        int donationCount = sharedPref.getInt(KEY_DONATION_COUNT, 0);
        instance = new Donor(id, businessName, address, contactFName, contactLName, phone, position, donationType, donationCount);
        return instance;
    }

    //getters
    public String getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getAddress() {
        return address;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public LatLng getPosition() {
        return position;
    }

    public Type getDonationType() {
        return donationType;
    }

    public int getDonationCount() {
        return donationCount;
    }


    //setters
    public void setAddress(Context context, LatLng latLng, String address) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_ADDRESS, address);
        editor.putFloat(Donor.KEY_LAT, (float) latLng.latitude);
        editor.putFloat(Donor.KEY_LNG, (float) latLng.longitude);
        editor.apply();

        this.address = address;
        this.position = latLng;
    }

    public void setTypeByString(Context context, String typeStr) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        prefs.edit().putString(Donor.KEY_TYPE, typeStr).apply();
        this.donationType  = TypeManager.get().getType(typeStr);
    }

    public void setBusinessName(Context context, String newBusinessName) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_BUIS_NAME, newBusinessName);
        editor.apply();
        this.businessName = newBusinessName;
    }

    public void setPhone(Context context, String contactPhone) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(Donor.KEY_PHONE, contactPhone);
        editor.apply();
        this.phone = contactPhone;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Exclude
    public String getContactInfo() {
        return firstName + " " + lastName;
    }

    @Exclude
    public void setContactInfo(Context context, String newContact) {
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

    private SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    public void addDonation(int delta) {
        Log.d(TAG, "add donation: " + delta);
        this.donationCount += delta;
    }

    public void clear() {
        instance = null;
        Log.d("BUG", "clear donor instance");
    }


    public void onStop(Context context) {
        Log.i("BUG", "onStop in Donor");
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(Donor.KEY_DONATION_COUNT, donationCount);
        editor.apply();
    }
}
