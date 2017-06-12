package com.gilshelef.feedme.nonprofit.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.NonProfit;
import com.gilshelef.feedme.nonprofit.data.OnBooleanResult;
import com.gilshelef.feedme.nonprofit.data.OnResult;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by gilshe on 3/21/17.
 */

public class NonProfitRegistrationActivity extends AppCompatActivity implements View.OnClickListener, OnBooleanResult {
    private static final String TAG = NonProfitRegistrationActivity.class.getSimpleName();
    public static final int REGISTER_NON_PROFIT = 1;

    private static DatabaseReference mNonProfitRef;
    private EditText mNonProfitName;
    private EditText mNonProfitNumber;
    private EditText mNonProfitAddress;
    private EditText mContactFirstName;
    private EditText mContactLastName;
    private EditText mContactId;
    private EditText mContactPhone;
    private LatLng mLatLng;
    private Logger mLogger;
    private String mNonProfitId;
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_non_profit);
        Util.loadPreference(this);

        mNonProfitRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_NON_PROFIT);

        //views
        mNonProfitName = (EditText) findViewById(R.id.non_profit_name);
        mNonProfitAddress = (EditText) findViewById(R.id.non_profit_address);
        mContactFirstName = (EditText) findViewById(R.id.non_profit_fname);
        mContactLastName = (EditText) findViewById(R.id.non_profit_lname);
        mContactId = (EditText) findViewById(R.id.contact_id);
        mContactPhone = (EditText) findViewById(R.id.contact_phone);
        mNonProfitNumber = (EditText) findViewById(R.id.non_profit_id);
        Button submitBtn = (Button) findViewById(R.id.submit_btn);

        // Click listeners
        submitBtn.setOnClickListener(this);

        mLogger = Logger.get(getApplicationContext());
    }


    private void finish(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.submit_btn){
            if(!validateForm())
                return;

            // checking phone
            if(!RegistrationHandler.checkPhone(this, mContactPhone.getText().toString()))
                return;

            //check id
            if(!validateId())
                return;

            //checking position
            mProgress = Util.buildProgressDialog(this);
            mProgress.show();
            mLatLng = RegistrationHandler.getLocationFromAddress(this, mNonProfitAddress);
            if(mLatLng == null) {
                if(mProgress.isShowing())
                    mProgress.dismiss();
                return;
            }

            //send authorization email to non-profit
            new SendAuthorizationEmailTask(this).execute();
        }
    }

    private boolean validateId() {
        String id = mContactId.getText().toString();
        if(id.length() != 9){
            mContactId.setError(getString(R.string.error_id));
            return false;
        }
        return true;
    }

    private boolean validateForm() {
        return  !RegistrationHandler.isEmpty(mNonProfitName) &&
                !RegistrationHandler.isEmpty(mNonProfitAddress) &&
                !RegistrationHandler.isEmpty(mContactFirstName) &&
                !RegistrationHandler.isEmpty(mContactLastName) &&
                !RegistrationHandler.isEmpty(mContactId) &&
                !RegistrationHandler.isEmpty(mContactPhone) &&
                !RegistrationHandler.isEmpty(mNonProfitNumber);

    }

    @Override
    public void onResult(boolean result) {
        if(result) {
            writeNewNonProfit();
            Util.notifyEmailSent(this, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(mProgress != null && mProgress.isShowing())
                        mProgress.dismiss();
                    finish(RESULT_OK);
                }
            });
            mLogger.signUp(Logger.EVENT.NON_PROFIT, mNonProfitId);
        }
    }

    private void writeNewNonProfit() {
        String contactName = mContactFirstName.getText().toString() + " " + mContactLastName.getText().toString();
        NonProfit nonProfit = new NonProfit(
                mNonProfitId,
                mLatLng,
                mNonProfitName.getText().toString(),
                contactName,
                mContactPhone.getText().toString(),
                mNonProfitAddress.getText().toString(),
                false
        );

        //to database
        mNonProfitRef.child(mNonProfitId).setValue(nonProfit);

        // to shared prefs
        SharedPreferences prefs = getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NonProfit.KEY_NAME, mNonProfitName.getText().toString());
        editor.putString(NonProfit.KEY_CONTACT, contactName);
        editor.putString(NonProfit.KEY_PHONE, mContactPhone.getText().toString());
        editor.putString(NonProfit.KEY_ADDRESS, mNonProfitAddress.getText().toString());
        editor.putFloat(NonProfit.KEY_LAT, (float) mLatLng.latitude);
        editor.putFloat(NonProfit.KEY_LNG, (float) mLatLng.longitude);
        editor.putString(NonProfit.KEY_ID, mNonProfitId);
        editor.putBoolean(NonProfit.KEY_AUTHORIZED, false);
        editor.apply();

    }

    private class SendAuthorizationEmailTask extends AsyncTask<Void, Void, Void> {
        private String email;
        private final OnBooleanResult callback;
        private static final String E_CONTACT_NAME = "name";
        private static final String E_CONTACT_ID = "contact_id";
        private static final String E_NON_PROFIT_ID = "non_profit_id";
        private static final String E_NON_PROFIT = Constants.DB_NON_PROFIT;
        private static final String E_TO = "to";
        private static final String E_NON_PROFIT_NUMBER = "non_profit_number";

        SendAuthorizationEmailTask(OnBooleanResult callback) {
            this.email = Constants.DEFAULT_EMAIL;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final String nonProfitNumber = mNonProfitNumber.getText().toString();
            final OnResult getEmailCallback = new OnResult() {
                @Override
                public void onResult() {
                    sendEmail();
                    callback.onResult(true);
                }
            };

            ValueEventListener getNonProfitEmail = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child: dataSnapshot.getChildren()){
                        String id = child.getKey();
                        if(id.equals(nonProfitNumber)) {
                            email = child.getValue(String.class);
                            break;
                        }
                    }
                    getEmailCallback.onResult();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.DB_NON_PROFIT_LISTED);
            ref.addListenerForSingleValueEvent(getNonProfitEmail);
            return null;
        }

        private void sendEmail() {

//            email = Constants.DEFAULT_EMAIL;
//            //TODO: remove line above


            //generate email information
            String contactName = mContactFirstName.getText().toString() + " " + mContactLastName.getText().toString();
            String contactId = mContactId.getText().toString();
            mNonProfitId = mNonProfitRef.push().getKey();
            Map<String, Object> updates = new HashMap<>();
            updates.put(E_CONTACT_NAME, contactName);
            updates.put(E_CONTACT_ID, contactId);
            updates.put(E_NON_PROFIT_ID, mNonProfitId);
            updates.put(E_TO, email);
            updates.put(E_NON_PROFIT, mNonProfitName.getText().toString());
            updates.put(E_NON_PROFIT_NUMBER, mNonProfitNumber.getText().toString());

            //send email message
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.DB_EMAILS);
            String key = ref.push().getKey();
            ref.child(key).updateChildren(updates);
        }
    }
}
