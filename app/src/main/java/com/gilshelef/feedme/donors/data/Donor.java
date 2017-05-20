package com.gilshelef.feedme.donors.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Exclude
    private LatLng position;
    private String businessName;
    private String address;
    private String phone;
    @Exclude
    private Type donationType;
    private String firstName;
    private String lastName;
    @Exclude
    private AtomicInteger donationCount;


    public Donor(){}
    public Donor(String id, String businessName, String address, String contactFName, String contactLName, String phone, LatLng position, Type donationType, int donationCount){
        this.id = id;
        this.businessName = businessName;
        this.address = address;
        this.firstName = contactFName;
        this.lastName = contactLName;
        this.phone = phone;
        this.position = position;
        this.donationType = donationType;
        this.donationCount = new AtomicInteger(donationCount);
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
        Log.d(TAG, "build in Donor");

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

    @Exclude
    public LatLng getPosition() {
        return position;
    }

    @Exclude
    public Type getDonationType() {
        return donationType;
    }

    @Exclude
    public int getDonationCount() {
        return donationCount.get();
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

    @Exclude
    public void setTypeByString(Context context, String typeStr) {
        getEditor(context).putString(Donor.K_TYPE, typeStr).apply();
        donationType  = TypeManager.get().getType(typeStr);
    }

    @Exclude
    public void setType(Type type) {
        donationType  = type;
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
        Log.d(TAG, "update donations count: " + delta);
        donationCount.addAndGet(delta);
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.DB_DONOR_DONATION)
                .child(getId())
                .child(Donor.K_DONATION_COUNT)
                .setValue(donationCount.get());
        getEditor(context).putInt(Donor.K_DONATION_COUNT, donationCount.get()).apply();
    }

    public static void clear() {
        synchronized (Donor.class) {
            instance = null;
        }
    }

    public boolean isOwner(Donation d) {
        return d.getDonorId().equals(getId());
    }

    public void setProfileInfo(Donation donation) {
        donation.setType(getDonationType());
        donation.setPhone(getPhone());
        donation.setFirstName(getFirstName());
        donation.setLastName(getLastName());
        donation.setPosition(getPosition());
        donation.setBusinessName(getBusinessName());
        donation.setDonorId(getId());

    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
