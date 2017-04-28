package com.gilshelef.feedme.donors.activities;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationDonorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final int REGISTER_DONOR = 2;
    private DatabaseReference mDatabase;

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
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

            //checking location
            mLatLng = RegistrationHandler.getLocationFromAddress(getApplicationContext(), mDonorAddress);
            if(mLatLng == null)
                return;

            writeNewDonor();
        }
    }


    private void writeNewDonor() {
        String uuid = UUID.randomUUID().toString();
        String donationTypeStr = mSpinner.getSelectedItem().toString();

        Donor donor = new Donor(
                uuid,
                mBusinessName.getText().toString(),
                mDonorAddress.getText().toString(),
                mContactFirstName.getText().toString(),
                mContactLastName.getText().toString(),
                mContactPhone.getText().toString(),
                mLatLng,
                TypeManager.get().getType(donationTypeStr),
                0
        );

        mDatabase.child(Constants.DB_DONOR_KEY).child(uuid).setValue(donor);

        SharedPreferences prefs = getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Donor.KEY_BUS_NAME, mBusinessName.getText().toString());
        editor.putString(Donor.KEY_FIRST_NAME, mContactFirstName.getText().toString());
        editor.putString(Donor.KEY_LAST_NAME, mContactLastName.getText().toString());
        editor.putString(Donor.KEY_PHONE, mContactPhone.getText().toString());
        editor.putString(Donor.KEY_ADDRESS, mDonorAddress.getText().toString());
        editor.putFloat(Donor.KEY_LAT, (float) mLatLng.latitude);
        editor.putFloat(Donor.KEY_LNG, (float) mLatLng.longitude);
        editor.putString(Donor.KAY_TYPE, donationTypeStr);
        editor.putString(Donor.KEY_UUID, uuid);
        editor.putInt(Donor.KEY_DONATION_COUNT, 0);
        editor.apply();

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
