package com.gilshelef.feedme.nonprofit.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.gilshelef.feedme.nonprofit.data.types.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gilshe on 3/10/17.
 */

public class Filter implements Parcelable {

    private Set<Type> donationTypes;
    private Set<String> businessName;
    private double maxDistance;


    private Filter(Parcel in){
        donationTypes = new HashSet<>();
        donationTypes.addAll(in.readArrayList(null));
        businessName = new HashSet<>();
        businessName.addAll(in.readArrayList(null));
        maxDistance = in.readDouble();
    }
    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        List<Type> types = new ArrayList<>();
        types.addAll(donationTypes);
        dest.writeList(types);

        List<String> names = new ArrayList<>();
        names.addAll(businessName);
        dest.writeList(names);

        dest.writeDouble(maxDistance);
    }
}
