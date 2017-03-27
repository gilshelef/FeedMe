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
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationDonorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int REGISTER_DONOR = 2;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_donor);

        final Spinner spinner = (Spinner) findViewById(R.id.donation_type_spinner);
        List<Type> typesArray = TypeManager.get().getAll();
        ArrayAdapter<Type> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, typesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        prefs = getSharedPreferences(RegistrationActivity.DONOR, Context.MODE_PRIVATE);

        final EditText businessName = (EditText) findViewById(R.id.donor_business_name);
        final EditText contactName = (EditText) findViewById(R.id.donor_name);
        final EditText donorAddress = (EditText) findViewById(R.id.donor_address);
        final EditText contactPhone = (EditText) findViewById(R.id.donor_phone);

        Button submit = (Button) findViewById(R.id.submit_btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!RegistrationHandler.isEmpty(businessName) &&
                        !RegistrationHandler.isEmpty(contactName) &&
                        !RegistrationHandler.isEmpty(donorAddress) &&
                        !RegistrationHandler.isEmpty(contactPhone) ){

                    // checking phone
                    if(!RegistrationHandler.checkPhone(getApplicationContext(), contactPhone.getText().toString()))
                        return;

                    //checking location
                    LatLng latLng = RegistrationHandler.getLocationFromAddress(getApplicationContext(),
                            donorAddress.getText().toString());
                    if(latLng == null)
                        return;

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Donor.KEY_BUS_NAME, businessName.getText().toString());
                    editor.putString(Donor.KEY_CONTACT, contactName.getText().toString());
                    editor.putString(Donor.KEY_PHONE, contactPhone.getText().toString());
                    editor.putString(Donor.KEY_ADDRESS, donorAddress.getText().toString());
                    editor.putFloat(Donor.KEY_LAT, (float) latLng.latitude);
                    editor.putFloat(Donor.KEY_LNG, (float) latLng.longitude);
                    editor.apply();

                    finish(RESULT_OK);
                }
            }
        });

    }

    private void finish(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String donationType = parent.getItemAtPosition(position).toString();
        prefs.edit().putString(Donor.KAY_TYPE, donationType).apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(getApplicationContext(), "נא לבחור סוג תרומה אחד לפחות", Toast.LENGTH_LONG).show();
    }
}
