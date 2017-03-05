package com.gilshelef.feedme;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gilshe on 2/21/17.
 */
class Donation implements Parcelable {

    public void setSelected(boolean val) {

        selected = val;
    }

    enum State {AVAILABLE, SAVED}

    String type; // donation type - vegetables, pasty etc
    String description;
    String imageUrl;
    String phone;
    String firstName;
    String lastName;
    String date;
    int defaultImage;
    private String id; // donation id
    private State state;
    Location location;
    private boolean selected;

    Donation(){
        selected = false;
    }

    String getType() {
        return type;
    }

    String getDescription() {
        return description;
    }

    String getContactInfo() {
        return firstName + " " + lastName;
    }

    String getImageUrl() {
        return imageUrl;
    }

    int getDefaultImage() {
        return defaultImage;
    }

    String getPhone() {
        return phone;
    }

    void setId(String id) {
        this.id = id;
    }

    void setState(State state) {
        this.state = state;
    }

    Location getLocation() {
        return location;
    }

    boolean isAvailable() {
        return state.equals(State.AVAILABLE);
    }

    boolean isSaved() {
        return state.equals(State.SAVED);
    }

//    boolean isSelected() {
//        return state.equals(State.SELECTED);
//    }

    boolean isSelected(){
        return selected;
    }

    public String getId() {
        return id;
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
            Donation mBook = new Donation();
            mBook.type = source.readString();
            mBook.imageUrl = source.readString();
            mBook.defaultImage = source.readInt();
            return mBook;
        }
        public Donation[] newArray(int size) {
            return new Donation[size];
        }
    };

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(type);
        parcel.writeString(imageUrl);
        parcel.writeInt(defaultImage);

    }


}
