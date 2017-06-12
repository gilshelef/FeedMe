package com.gilshelef.feedme.donors.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.launcher.RegistrationActivity;
import com.gilshelef.feedme.launcher.RegistrationHandler;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationDonorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final int REGISTER_DONOR = 2;
    private static final String TAG = RegistrationDonorActivity.class.getSimpleName();
    private DatabaseReference mDonorRef;

    private EditText mBusinessName;
    private EditText mContactFirstName;
    private EditText mContactLastName;
    private EditText mDonorAddress;
    private EditText mContactPhone;
    private LatLng mLatLng;
    private Spinner mSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_donor);
        Util.loadPreference(this);

        mDonorRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_DONOR);

        mSpinner = (Spinner) findViewById(R.id.donation_type_spinner);
        List<Type> typesArray = TypeManager.get().getAll();
        Collections.sort(typesArray, new TypeManager.TypeComparator());
        ArrayAdapter<Type> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, typesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        //views
        mBusinessName = (EditText) findViewById(R.id.donor_business_name);
        mContactFirstName = (EditText) findViewById(R.id.donor_fname);
        mContactLastName = (EditText) findViewById(R.id.donor_lname);
        mDonorAddress = (EditText) findViewById(R.id.donor_address);
        mContactPhone = (EditText) findViewById(R.id.donor_phone);

        Button submit = (Button) findViewById(R.id.submit_btn);
        submit.setOnClickListener(this);

    }


    private void finish(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        String donationType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(getApplicationContext(), "נא לבחור סוג תרומה אחד לפחות", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.submit_btn) {
            if(!validateForm())
                return;

            // checking phone
            if(!RegistrationHandler.checkPhone(getApplicationContext(), mContactPhone.getText().toString()))
                return;

            //checking position
            ProgressDialog progress = Util.buildProgressDialog(this);
            progress.show();
            mLatLng = RegistrationHandler.getLocationFromAddress(this, mDonorAddress);
            if(mLatLng == null) {
                progress.dismiss();
                return;
            }

            writeNewDonor();
            if(progress.isShowing())
                progress.dismiss();
        }
    }


    private void writeNewDonor() {
        String donationTypeStr = mSpinner.getSelectedItem().toString();
        Type donationType = TypeManager.get().getType(donationTypeStr);
        String donorId = mDonorRef.push().getKey();

        Donor donor = new Donor(
                donorId,
                mBusinessName.getText().toString(),
                mDonorAddress.getText().toString(),
                mContactFirstName.getText().toString(),
                mContactLastName.getText().toString(),
                mContactPhone.getText().toString(),
                mLatLng,
                donationType,
                0 // initial donationCount
        );

        // to database
        Map<String, Object> donorValues = donor.toMap();
        mDonorRef.child(donorId).updateChildren(donorValues);


        // to shared prefs
        SharedPreferences prefs = getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Donor.K_BUSINESS, mBusinessName.getText().toString());
        editor.putString(Donor.K_FIRST_NAME, mContactFirstName.getText().toString());
        editor.putString(Donor.K_LAST_NAME, mContactLastName.getText().toString());
        editor.putString(Donor.K_PHONE, mContactPhone.getText().toString());
        editor.putString(Donor.K_ADDRESS, mDonorAddress.getText().toString());
        editor.putFloat(Donor.K_LAT, (float) mLatLng.latitude);
        editor.putFloat(Donor.K_LNG, (float) mLatLng.longitude);
        editor.putString(Donor.K_TYPE, donationTypeStr);
        editor.putString(Donor.K_ID, donorId);
        editor.putInt(Donor.K_DONATION_COUNT, 0);
        editor.apply();

        Logger.get(this).signUp(Logger.EVENT.DONOR, donorId);
        finish(RESULT_OK);
    }

    private boolean validateForm() {
        return  !RegistrationHandler.isEmpty(mBusinessName) &&
                !RegistrationHandler.isEmpty(mContactFirstName) &&
                !RegistrationHandler.isEmpty(mContactLastName) &&
                !RegistrationHandler.isEmpty(mDonorAddress) &&
                !RegistrationHandler.isEmpty(mContactPhone);
    }
}
