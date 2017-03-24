package com.gilshelef.feedme.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.gilshelef.feedme.data.types.Type;
import com.gilshelef.feedme.data.types.TypeManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gilshe on 2/21/17.
 */
public class Donation implements Parcelable{

    private static final String TAG = Donation.class.getSimpleName();

    public enum State {AVAILABLE, SAVED, OWNED}

    // minimum qualification for donation
    private Type type;
    private String phone;
    private String firstName;
    private String lastName;
    private LatLng location;
    private String businessName;
    private String id; // donation id

    private String description;
    private String imageUrl;
    private State state;
    private boolean inCart;

    private Donation(){
    }

    public boolean isOwned() {return state == State.OWNED;}
    public boolean inCart() {
        return inCart;
    }
    public State getState() {
        return state;
    }

    //TODO add time to take donation
    public Donation(JSONObject obj) {
        inCart = false;
        try {
            phone = obj.getString("phone");
            firstName = obj.getString("firstName");
            lastName = obj.getString("lastName");
            id = obj.getString("id");
            type = TypeManager.get().getType(obj.getString("type"));
            location = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
            description = obj.getString("description");
            imageUrl = obj.getString("imageUrl");
            businessName = obj.getString("businessName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    void setId(String id) {
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

    public String getTime() {
        return "חמישי 14:00 - 16:00"; // TODO
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
    }
}
