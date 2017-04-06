package com.gilshelef.feedme.nonprofit.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;

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



    public enum State {AVAILABLE, SAVED, OWNED, DONOR}

    // minimum qualification for donation
    public Type type;
    public String phone;
    public String firstName;
    public String lastName;
    public LatLng location;
    public String businessName;
    private String id; // donation id

    public Calendar calendar;
    public String description;
    public String imageUrl;
    private State state;
    private boolean inCart;

    public Donation(){
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
            location = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));

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

    public boolean isDonor() {
        return state == State.DONOR;
    }
    public boolean isOwned() {return state == State.OWNED;}
    public boolean inCart() {
        return inCart;
    }
    public State getState() {
        return state;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setCalendar(String calender) {
        this.calendar = stringToCalender(calender);
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

    public String getBusinessName() {
        return businessName;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getContactInfo() {
        return firstName + " " + lastName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setState(State state) {
        this.state = state;
    }

    public LatLng getPosition() {
        return location;
    }

    public boolean isAvailable() {
        return state.equals(State.AVAILABLE);
    }

    public boolean isSaved() {
        return state.equals(State.SAVED);
    }

    public boolean isInCart(){
        return inCart;
    }

    public String getId() {
        return id;
    }

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

    public void setInCart(boolean val) {
        inCart = val;
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
            donation.location = new LatLng(latitude, longitude);
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
        parcel.writeFloat((float)location.latitude);
        parcel.writeFloat((float)location.longitude);
        parcel.writeString(businessName);
        parcel.writeString(description);
        parcel.writeString(imageUrl);
        parcel.writeString(state.name());
        parcel.writeString(id);
        parcel.writeString(inCart+"");
        parcel.writeString(calenderToString());
    }
}
