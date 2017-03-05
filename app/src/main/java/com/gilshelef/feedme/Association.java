package com.gilshelef.feedme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

/**
 * Created by gilshe on 2/25/17.
 */
class Association {

    private static Association instance;
    private static String uuid;
    private static Location baseLocation;

    private static final int KILOMETER = 1000;


    private Association(String id, Location location){
        uuid = id;
        baseLocation = location;
    }

    static Association get(Activity activity) {
        if (instance == null) {
            synchronized (Association.class) {
                if (instance == null)
                    return build(activity);
            }
        }
        return instance;
    }

    private static Association build(Activity activity) {

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String uuid = sharedPref.getString("key_uuid", "1"); // TODO change to null after registering
        double latitude = sharedPref.getFloat("key_latitude", 31);
        double longitude = sharedPref.getFloat("key_longitude", 34);

        Location baseLocation = new Location("base_location");
        baseLocation.setLatitude(latitude);
        baseLocation.setLongitude(longitude);

        instance = new Association(uuid, baseLocation);
        return instance;


    }

    static float calcDistance(Location location) {
        return (location.distanceTo(baseLocation))/ KILOMETER;
    }

    public static String getId() {
        return uuid;
    }

}
