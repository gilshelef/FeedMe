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

    public static LatLng getLocationFromAddress(Context context, EditText input) {
        String strAddress = input.getText().toString();
        Locale lHebrew = new Locale.Builder().setLanguage(HEBREW).build();
        Geocoder geocoder = new Geocoder(context, lHebrew);
        List<Address> address;
        LatLng latLng = null;
        try {
            //TODO move to async task
            address = geocoder.getFromLocationName(strAddress, 1);
            if (address == null || address.size() == 0) {
                input.setError(RESULT_UNKNOWN_LOCATION);
                return null;
            }

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            input.setError("נראה שיש בעיות בחיבור לאינטרנט, נא נסה שנית מאוחר יותר");
        }
        return latLng;
    }

    public static boolean isNonProfitListed(String nonProfitName){
        //TODO check with DB for non profit name
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
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
            return isNonProfitListed(nonProfitName);
        }

        @Override
        protected void onPostExecute(Boolean listed){
            if(progress != null) progress.dismiss();
            if(!listed)
                Toast.makeText(activity, RESULT_UNKNOWN_NON_PROFIT, Toast.LENGTH_LONG).show();
            callback.onResult(listed);
        }
    }
//    private static class ResolveAddressTask extends AsyncTask<Void, Void, Boolean> {
//        private final String strAddress;
//        private final EditText inputView;
//        private final Context context;
//        private ProgressDialog progress;
//
//        private ResolveAddressTask(String strAddress, EditText input, Context context) {
//            this.strAddress = strAddress;
//            this.inputView = input;
//            this.context = context;
//        }
//
//        @Override
//        protected void onPreExecute(){
//
//            progress = new ProgressDialog(context);
//            progress.setTitle(context.getString(R.string.please_wait));
//            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progress.setIndeterminate(true);
//            progress.setCanceledOnTouchOutside(false);
//            progress.show();
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            Locale lHebrew = new Locale.Builder().setLanguage(HEBREW).build();
//            Geocoder geocoder = new Geocoder(context, lHebrew);
//            List<Address> address;
//            LatLng latLng = null;
//            try {
//                address = geocoder.getFromLocationName(strAddress, 1);
//                if (address == null || address.size() == 0)
//                    return null;
//
//                Address position = address.get(0);
//                latLng = new LatLng(position.getLatitude(), position.getLongitude());
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage());
//                input.setError("it seems you have connection error, please try again later");
//            }finally {
//                if(latLng == null)
//                    input.setError(RESULT_UNKNOWN_LOCATION);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean listed){
//            if(progress != null) progress.dismiss();
//            if(listed) {
//                editor.putString(NonProfit.K_ID, uuid);
//                editor.apply();
//                finish(RESULT_OK);
//            }
//            else
//                Toast.makeText(getApplicationContext(), RegistrationHandler.RESULT_UNKNOWN_NON_PROFIT, Toast.LENGTH_LONG).show();
//
//        }
//    }



}
