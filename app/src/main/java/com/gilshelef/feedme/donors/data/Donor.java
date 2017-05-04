package com.gilshelef.feedme.donors.data;

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

    public static final String K_BUSINESS = "businessName";
    public static final String K_FIRST_NAME = "firstName";
    public static final String K_LAST_NAME = "lastName";
    public static final String K_PHONE = "phone";
    public static final String K_LAT = "latitude";
    public static final String K_LNG = "longitude";
    public static final String K_ID = "id";
    public static final String K_ADDRESS = "address";
    public static final String K_TYPE = "donationType";
    public static final String K_DONATION_COUNT = "donationCount";
    public static final String K_POSITION = "position";

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

    public static Donor get(Context context) {
        if (instance == null) {
            synchronized (Donor.class) {
                if (instance == null)
                    return build(context);
            }
        }
        return instance;
    }

    private static Donor build(Context context) {
        Log.d("BUG", "build in Donor");

        SharedPreferences sharedPref = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        String id = sharedPref.getString(K_ID, "0");
        String businessName = sharedPref.getString(K_BUSINESS, "");
        String address = sharedPref.getString(K_ADDRESS, "");
        float latitude = sharedPref.getFloat(K_LAT, 0);
        float longitude = sharedPref.getFloat(K_LNG, 0);
        String contactFName = sharedPref.getString(K_FIRST_NAME, "");
        String contactLName = sharedPref.getString(K_LAST_NAME, "");
        String phone = sharedPref.getString(K_PHONE, "");
        Type donationType = TypeManager.get().getType(sharedPref.getString(K_TYPE, TypeManager.OTHER_DONATION));
        LatLng position = new LatLng(latitude, longitude);
        int donationCount = sharedPref.getInt(K_DONATION_COUNT, 0);
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
        editor.putString(Donor.K_ADDRESS, address);
        editor.putFloat(Donor.K_LAT, (float) latLng.latitude);
        editor.putFloat(Donor.K_LNG, (float) latLng.longitude);
        editor.apply();

        this.address = address;
        this.position = latLng;
    }

    public void setTypeByString(Context context, String typeStr) {
        getEditor(context).putString(Donor.K_TYPE, typeStr).apply();
        this.donationType  = TypeManager.get().getType(typeStr);
    }

    public void setBusinessName(Context context, String newBusinessName) {
        getEditor(context).putString(Donor.K_BUSINESS, newBusinessName).apply();
        this.businessName = newBusinessName;
    }

    public void setPhone(Context context, String phone) {
        getEditor(context).putString(Donor.K_PHONE, phone).apply();
        this.phone = phone;
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
        editor.putString(Donor.K_FIRST_NAME, firstName);
        editor.putString(Donor.K_LAST_NAME, lastName);
        editor.apply();
    }

    private SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    public void updateDonationCount(Context context, int delta) {
        Log.d(TAG, "add donation: " + delta);
        this.donationCount += delta;
        getEditor(context).putInt(Donor.K_DONATION_COUNT, donationCount).apply();
    }

    public void clear() {
        instance = null;
    }

}
