package com.gilshelef.feedme.launcher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Association;
import com.gilshelef.feedme.nonprofit.data.OnBooleanResult;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by gilshe on 3/26/17.
 */

public class RegistrationHandler {
    public static final String RESULT_UNKNOWN_NON_PROFIT = "עמותה לא מזוהה מול רשם העמותות";
    private static final String RESULT_UNKNOWN_LOCATION = "מיקום לא נמצא, נא להכניס שנית";
    private static final String RESULT_ERROR_PHONE = "מס' טלפון אינו נכון";
    private static final String HEBREW = "he";
    private static final String TAG = RegistrationHandler.class.getSimpleName();

    public static boolean checkPhone(Context context, String phone) {
        if(phone.length() != 10){
            Toast.makeText(context, RESULT_ERROR_PHONE, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        Locale lHebrew = new Locale.Builder().setLanguage(HEBREW).build();
        Geocoder geocoder = new Geocoder(context, lHebrew);
        List<Address> address;
        LatLng latLng = null;
        try {
            //TODO move to async task
            address = geocoder.getFromLocationName(strAddress, 1);
            if (address == null)
                return null;

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }finally {
            if(latLng == null)
                Toast.makeText(context, RESULT_UNKNOWN_LOCATION, Toast.LENGTH_LONG).show();
        }

        return latLng;
    }

    public static String checkForNonProfitListing(String nonProfitName){
        String uuid = "1";
        //TODO check with DB for non profit name
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return uuid;
    }


    public static boolean isEmpty(EditText field) {
        boolean empty = false;
        if(field.getText().toString().trim().equals("")){
            empty = true;
            field.setError(" נא למלא שדה זה ");

        }
        return empty;
    }

    public static class CheckForNonProfitListingTask extends AsyncTask<Void, Void, Boolean> {
        private final OnBooleanResult callback;
        private ProgressDialog progress;
        private String nonProfitName;
        private Activity activity;
        String uuid;

        public CheckForNonProfitListingTask(Activity activity, String nonProfitName, OnBooleanResult callback) {
            this.nonProfitName = nonProfitName;
            this.activity = activity;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute(){
            progress = new ProgressDialog(activity);
            progress.setTitle(activity.getString(R.string.please_wait));
            progress.setMessage("מאמתים את שם העמותה מול רשם העמותות");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //TODO check with DB for non profit name
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            uuid = "1";
            return true;
        }

        @Override
        protected void onPostExecute(Boolean listed){
            if(progress != null) progress.dismiss();
            if(listed)
                Association.get(activity).setNonProfitName(activity, nonProfitName, uuid);

            else
                Toast.makeText(activity, RESULT_UNKNOWN_NON_PROFIT, Toast.LENGTH_LONG).show();
            callback.onResult(listed);
        }
    }



}
