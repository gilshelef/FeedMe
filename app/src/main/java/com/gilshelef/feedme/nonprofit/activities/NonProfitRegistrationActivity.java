package com.gilshelef.feedme.nonprofit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;


/**
 * Created by gilshe on 3/21/17.
 */

public class NonProfitRegistrationActivity extends AppCompatActivity implements View.OnClickListener, OnBooleanResult {
    public static final int REGISTER_NON_PROFIT = 1;
    private DatabaseReference mDatabase;
    private EditText mNonProfitName;
    private EditText mNonProfitAddress;
    private EditText mContactName;
    private EditText mContactPhone;
    private LatLng mLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_non_profit);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //views
        mNonProfitName = (EditText) findViewById(R.id.non_profit_name);
        mNonProfitAddress = (EditText) findViewById(R.id.non_profit_address);
        mContactName = (EditText) findViewById(R.id.contact_name);
        mContactPhone = (EditText) findViewById(R.id.contact_phone);
        Button submitBtn = (Button) findViewById(R.id.submit_btn);

        // Click listeners
        submitBtn.setOnClickListener(this);
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
            if(!RegistrationHandler.checkPhone(getApplicationContext(), mContactPhone.getText().toString()))
                return;

            //checking location
            mLatLng = RegistrationHandler.getLocationFromAddress(getApplicationContext(), mNonProfitAddress);
            if(mLatLng == null)
                return;

            //checking non profit listing
            new RegistrationHandler.CheckForNonProfitListingTask(this, mNonProfitName.getText().toString(), this).execute();
        }
    }


    private boolean validateForm() {
        return  !RegistrationHandler.isEmpty(mNonProfitName) &&
                !RegistrationHandler.isEmpty(mNonProfitAddress) &&
                !RegistrationHandler.isEmpty(mContactName) &&
                !RegistrationHandler.isEmpty(mContactPhone);

    }

    @Override
    public void onResult(boolean result) {
        if(result)
            writeNewNonProfit();

    }

    private void writeNewNonProfit() {
        String uuid = UUID.randomUUID().toString();
        NonProfit nonProfit = new NonProfit(
                uuid,
                mLatLng,
                mNonProfitName.getText().toString(),
                mContactName.getText().toString(),
                mContactPhone.getText().toString(),
                mNonProfitAddress.getText().toString());

        //to database
        mDatabase.child(Constants.DB_NON_PROFIT_KEY).child(uuid).setValue(nonProfit);

        // to shared prefs
        SharedPreferences prefs = getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NonProfit.KEY_NAME, mNonProfitName.getText().toString());
        editor.putString(NonProfit.KEY_CONTACT, mContactName.getText().toString());
        editor.putString(NonProfit.KEY_PHONE, mContactPhone.getText().toString());
        editor.putString(NonProfit.KEY_ADDRESS, mNonProfitAddress.getText().toString());
        editor.putFloat(NonProfit.KEY_LAT, (float) mLatLng.latitude);
        editor.putFloat(NonProfit.KEY_LNG, (float) mLatLng.longitude);
        editor.putString(NonProfit.KEY_UUID, uuid);
        editor.apply();


        finish(RESULT_OK);

    }
}
