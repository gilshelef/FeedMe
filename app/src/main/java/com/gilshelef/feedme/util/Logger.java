package com.gilshelef.feedme.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by gilshe on 5/27/17.
 */

public class Logger {
    private final FirebaseAnalytics mFirebaseAnalitics;
    private static Logger instance;

    public static Logger get(Context context) {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger(context);
                }
            }
        }
        return instance;
    }

    private Logger(Context context) {
        mFirebaseAnalitics = FirebaseAnalytics.getInstance(context);
    }

    public void takeDonation(String donationId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, donationId);
        mFirebaseAnalitics.logEvent(EVENT.TAKE_DONATION, bundle);
    }


    public class EVENT {
        public static final String NEW_DONATION = "NEW_DONATION";
        public static final String RETURN_DONATION = "RETURN_DONATION";
        public static final String DONOR = "DONOR";
        public static final String NON_PROFIT = "NON_PROFIT";
        public static final String USER_TYPE = "NON_PROFIT";
        public static final String OWN_DONATION = "OWN_DONATION";
        public static final String TAKE_DONATION = "TAKE_DONATION";
    }


    public void signUp(String userType, String id) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(EVENT.USER_TYPE, userType);
        mFirebaseAnalitics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }

    public void removeRegistration(String userType, String id){
        Bundle bundle = new Bundle();
        bundle.putString(EVENT.USER_TYPE, userType);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        mFirebaseAnalitics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }

    public void newDonation(String donationId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, donationId);
        mFirebaseAnalitics.logEvent(EVENT.NEW_DONATION, bundle);
    }

    public void returnDonation(String donationId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, donationId);
        mFirebaseAnalitics.logEvent(EVENT.RETURN_DONATION, bundle);
    }

    public void ownDonation(String donationId) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, donationId);
        mFirebaseAnalitics.logEvent(EVENT.OWN_DONATION, bundle);
    }


}
