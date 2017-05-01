package com.gilshelef.feedme.nonprofit.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import org.json.JSONException;
import org.json.JSONObject;

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

    public enum State {AVAILABLE, SAVED, OWNED, DONOR, TAKEN, UNAVAILABLE}

    public Type type;
    public String phone;
    public String firstName;
    public String lastName;
    public LatLng position;
    public String businessName;
    private String id;
    public String donorId;

    public String description;
    public String imageUrl;
    private State state;
    private boolean inCart;


    @Exclude
    public Calendar calendar;


    public Donation(){
        inCart = false;
    }

    public Donation(JSONObject obj) {
        inCart = false;
        try {
            phone = safeStringGet(obj, "phone");
            firstName = safeStringGet(obj, "firstName");
            lastName = safeStringGet(obj, "lastName");
            id = safeStringGet(obj, "id");
            type = TypeManager.get().getType(safeStringGet(obj, "type"));
            description = safeStringGet(obj, "description");
            imageUrl = safeStringGet(obj, "imageUrl");
            businessName = safeStringGet(obj, "businessName");
            position = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));

            Locale locale = new Locale.Builder().setLanguage("he").build();
            String calenderStr = safeStringGet(obj, "calender");
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, locale);
            Date date = sdf.parse(calenderStr);// all done
            calendar = sdf.getCalendar();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //getters
    public Type getType() {
        return type;
    }
    public String getPhone() {
        return phone;
    }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    @Exclude
    public String getContactInfo() {
        return firstName + " " + lastName;
    }
    public LatLng getPosition() {
        return position;
    }
    public String getBusinessName() {
        return businessName;
    }
    public String getId() {
        return id;
    }
    public String getDonorId(){ return donorId; }

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

    private String safeStringGet(JSONObject obj, String key) {
        if(obj.has(key))
            try {
                return obj.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return "";
    }


    // state
    @Exclude
    public boolean isDonor() {
        return state == State.DONOR;
    }
    @Exclude
    public boolean isOwned() {return state == State.OWNED;}
    public boolean getInCart() {
        return inCart;
    }
    @Exclude
    public boolean isAvailable() {
        return state.equals(State.AVAILABLE);
    }
    @Exclude
    public boolean isSaved() {
        return state.equals(State.SAVED);
    }

    //setters
    public void setInCart(boolean val) {
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
    public void setState(State state) {
        this.state = state;
    }
    public void setDonorId(String donorId) { this.donorId = donorId; }

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
