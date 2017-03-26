package com.gilshelef.feedme.launcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Association;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationNonProfitActivity extends AppCompatActivity {
    public static final int REGISTER_NON_PROFIT = 1;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_non_profit);


        final EditText nonProfitName = (EditText) findViewById(R.id.non_profit_name);
        final EditText nonProfitAddress = (EditText) findViewById(R.id.non_profit_address);
        final EditText contactName = (EditText) findViewById(R.id.contact_name);
        final EditText contactPhone = (EditText) findViewById(R.id.contact_phone);

        submit = (Button) findViewById(R.id.submit_btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!RegistrationHandler.isEmpty(nonProfitName) &&
                        !RegistrationHandler.isEmpty(nonProfitAddress) &&
                        !RegistrationHandler.isEmpty(contactName) &&
                        !RegistrationHandler.isEmpty(contactPhone) ){

                    // checking phone
                    if(!RegistrationHandler.checkPhone(getApplicationContext(), contactPhone.getText().toString()))
                        return;

                    //checking location
                    LatLng latLng = RegistrationHandler.getLocationFromAddress(getApplicationContext(),
                            nonProfitAddress.getText().toString());
                    if(latLng == null)
                        return;

                    SharedPreferences prefs = getSharedPreferences(RegistrationActivity.PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Association.KEY_NAME, nonProfitName.getText().toString());
                    editor.putString(Association.KEY_CONTACT, contactName.getText().toString());
                    editor.putString(Association.KEY_PHONE, contactPhone.getText().toString());
                    editor.putString(Association.KEY_ADDRESS, nonProfitAddress.getText().toString());
                    editor.putFloat(Association.KEY_LAT, (float) latLng.latitude);
                    editor.putFloat(Association.KEY_LNG, (float) latLng.longitude);

                    //checking non profit listing
                    new CheckForNonProfitListingTask(editor, nonProfitName.getText().toString()).execute();
                }
            }
        });

    }


    private void finish(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }

    private class CheckForNonProfitListingTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progress;
        SharedPreferences.Editor editor;
        String nonProfitName;
        String uuid;

        private CheckForNonProfitListingTask(SharedPreferences.Editor editor, String nonProfitName) {
            this.editor = editor;
            this.nonProfitName = nonProfitName;
        }

        @Override
        protected void onPreExecute(){

            progress = new ProgressDialog(RegistrationNonProfitActivity.this);
            progress.setTitle(getString(R.string.please_wait));
            progress.setMessage("מאמתים את שם העמותה מול רשם העמותות");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            uuid = RegistrationHandler.checkForNonProfitListing(nonProfitName);
            return uuid != null;
        }

        @Override
        protected void onPostExecute(Boolean listed){
            if(progress != null) progress.dismiss();
            if(listed) {
                editor.putString(Association.KEY_UUID, uuid);
                editor.apply();
                finish(RESULT_OK);
            }
            else
                Toast.makeText(getApplicationContext(), RegistrationHandler.RESULT_UNKNOWN_NON_PROFIT, Toast.LENGTH_LONG).show();

        }
    }
}
