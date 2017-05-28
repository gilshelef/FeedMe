package com.gilshelef.feedme.nonprofit.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gilshe on 2/21/17.
 */
public class Donation implements Parcelable{

    private static final String TAG = Donation.class.getSimpleName();
    public static final String K_ID = "id";
    public static final String K_DONOR_ID = "donorId";
    public static final String K_NON_PROFIT_ID = "nonProfitId";
    public static final String K_DESCRIPTION = "description";
    public static final String K_IMAGE = "imageUrl";
    public static final String K_STATE = "state";
    public static final String K_CART = "inCart";
    public static final String K_CALENDAR = "calendar";
    public static final String K_TAKEN = "taken";

    public enum State {SAVED, DONOR, OWNED, AVAILABLE, TAKEN} //no use for UNAVAILABLE

    //donor's property
    @Exclude
    public Type type;
    @Exclude
    public String phone;
    @Exclude
    private String firstName;
    @Exclude
    private String lastName;
    @Exclude
    public LatLng position;
    @Exclude
    private String businessName;

    private String id;
    private String donorId;
    private String nonProfitId;
    private String description;
    private String imageUrl;
    private State state;
    private boolean inCart;

    //added separately
    @Exclude
    public Calendar calendar;

    public Donation(){
        inCart = false;
    }

    //getters
    @Exclude
    public Type getType() {
        return type;
    }
    @Exclude
    public String getPhone() {
        return phone;
    }
    @Exclude
    public String getFirstName() { return firstName; }
    @Exclude
    public String getLastName() { return lastName; }
    @Exclude
    public String getContactInfo() {
        return firstName + " " + lastName;
    }
    @Exclude
    public LatLng getPosition() {
        return position;
    }
    @Exclude
    public String getBusinessName() {
        return businessName;
    }

    public String getCalendar() {
        return calenderToString();
    }
    public String getId() {
        return id;
    }
    public String getDonorId(){ return donorId; }
    public String getNonProfitId(){ return nonProfitId; }
    public String calenderToString() {
        Locale locale = new Locale.Builder().setLanguage("he").build();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, locale);
        return sdf.format(calendar.getTime());
    }
    public static Calendar stringToCalender(String calenderStr){
        Calendar calendar = null;
        Locale locale = new Locale.Builder().setLanguage("he").build();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, locale);
        try {
            Date date = sdf.parse(calenderStr);// all done
            calendar = sdf.getCalendar();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar != null ? calendar : Calendar.getInstance();
    }
    public String getDescription() {
        return description;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public State getState() {
        return state;
    }


    // state
    public synchronized boolean getInCart() {
        return inCart;
    }
    @Exclude
    public boolean isDonor() {
        return state == State.DONOR;
    }
    @Exclude
    public boolean isOwned() {return state == State.OWNED;}
    @Exclude
    public synchronized boolean isAvailable() {
        return state.equals(State.AVAILABLE);
    }
    @Exclude
    public synchronized boolean isSaved() {
        return state.equals(State.SAVED);
    }

    //setters
    public synchronized void setInCart(boolean val) {
        inCart = val;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCalendar(String calender) {
        this.calendar = stringToCalender(calender);
    }
    public void setId(String id) {
        this.id = id;
    }
    public synchronized void setState(State state) {
        this.state = state;
    }
    public void setDonorId(String donorId) { this.donorId = donorId; }
    public void setNonProfitId(String nonProfitId){ this.nonProfitId = nonProfitId; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //donor's data
    public void setType(Type type) {this.type = type; }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPosition(LatLng position) {
        this.position = position;
    }
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void update(Donation other) {
        if(other == null)
            return;
        if (!getId().equals(other.getId())) {
            Log.e(TAG, "got update for different donations");
            return;
        }

        if (!getDonorId().equals(other.getDonorId())) {
            Log.e(TAG, "got update for different donors");
            return;
        }

        setDescription(other.getDescription());
        setImageUrl(other.getImageUrl());
        setState(other.getState());
        setCalendar(other.getCalendar());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Donation)) return false;

        Donation donation = (Donation) o;
        return donation.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    public static final Parcelable.Creator<Donation> CREATOR = new Creator<Donation>() {
        public Donation createFromParcel(Parcel source) {
            Donation donation = new Donation();
            donation.type = (Type) source.readSerializable();
            donation.phone = source.readString();
            donation.firstName = source.readString();
            donation.lastName = source.readString();
            float latitude = source.readFloat();
            float longitude = source.readFloat();
            donation.position = new LatLng(latitude, longitude);
            donation.businessName = source.readString();
            donation.description = source.readString();
            donation.imageUrl = source.readString();
            donation.state = State.valueOf(source.readString());
            donation.id = source.readString();
            donation.inCart = Boolean.valueOf(source.readString());
            donation.calendar = stringToCalender(source.readString());
            return donation;
        }
        public Donation[] newArray(int size) {
            return new Donation[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(type);
        parcel.writeString(phone);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeFloat((float) position.latitude);
        parcel.writeFloat((float) position.longitude);
        parcel.writeString(businessName);
        parcel.writeString(description);
        parcel.writeString(imageUrl);
        parcel.writeString(state.name());
        parcel.writeString(id);
        parcel.writeString(inCart+"");
        parcel.writeString(calenderToString());
    }
}
