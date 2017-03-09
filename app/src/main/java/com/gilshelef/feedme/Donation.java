package com.gilshelef.feedme;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 2/21/17.
 */
class Donation implements Parcelable{

    enum State {AVAILABLE, SAVED}

    private DonationType type;
    String description;
    String imageUrl;
    String phone;
    String firstName;
    String lastName;
    String date;
    private String id; // donation id
    private State state;
    LatLng location;
    private boolean selected;


    static class DonationType {
        private Type type;

        enum Type {Clothes, Vegetables, Pastries, Donation};
        private static final Map<Type, String> englishToHebrew = new HashMap<>();
        static {
            englishToHebrew.put(Type.Clothes, "בגדים");
            englishToHebrew.put(Type.Vegetables, "ירקות");
            englishToHebrew.put(Type.Pastries, "מאפים");
            englishToHebrew.put(Type.Donation, "תרומה");
        }

        private static final Map<String, Type> hebrewToEnglish = new HashMap<>();
        static {
            hebrewToEnglish.put("בגדים", Type.Clothes);
            hebrewToEnglish.put("ירקות", Type.Vegetables);
            hebrewToEnglish.put("מאפים", Type.Pastries);
            hebrewToEnglish.put("תרומה", Type.Donation);
        }

        private static final Map<Type, Integer> typeToImage = new HashMap<>();
        static {
            typeToImage.put(Type.Clothes, R.drawable.ic_clothes);
            typeToImage.put(Type.Vegetables, R.drawable.ic_vegetable);
            typeToImage.put(Type.Pastries, R.drawable.ic_cake);
            typeToImage.put(Type.Donation, R.drawable.placeholder);
        }

        private static final Map<Type, Float> typeToColor = new HashMap<>();
        static {
            typeToColor.put(Type.Clothes,  BitmapDescriptorFactory.HUE_RED);
            typeToColor.put(Type.Vegetables, 115f);
            typeToColor.put(Type.Pastries, 45f);
            typeToColor.put(Type.Donation, BitmapDescriptorFactory.HUE_AZURE);
        }

        DonationType(){}

        int getImage() {
            return typeToImage.get(type);
        }

        void setType(String typeHebrew) {
            this.type = hebrewToEnglish.get(typeHebrew);
        }

        String hebrew(){
            return englishToHebrew.get(type);
        }

        public float getColor() {
            return typeToColor.get(type);
        }
    }


    Donation(){
        selected = false;
        type = new DonationType();
    }

    DonationType getType() {
        return type;
    }

    void setType(String typeHebrew) {
        this.type.setType(typeHebrew);
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
        return type.getImage();
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

    LatLng getPosition() {
        return location;
    }

    boolean isAvailable() {
        return state.equals(State.AVAILABLE);
    }

    boolean isSaved() {
        return state.equals(State.SAVED);
    }

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

    void setSelected(boolean val) {
        selected = val;
    }

    public static final Parcelable.Creator<Donation> CREATOR = new Creator<Donation>() {
        public Donation createFromParcel(Parcel source) {
            Donation donation = new Donation();
            donation.setType(source.readString());
            donation.imageUrl = source.readString();
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
        parcel.writeString(type.hebrew());
        parcel.writeString(imageUrl);

    }

}
