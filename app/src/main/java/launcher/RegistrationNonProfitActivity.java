package launcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.Association;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationNonProfitActivity extends AppCompatActivity {
    public static final int REGISTER_NON_PROFIT = 1;
    private static final String RESULT_UNKNOWN_NON_PROFIT = "עמותה לא מזוהה מול רשם העמותות";
    private static final String RESULT_UNKNOWN_LOCATION = "מיקום לא נמצא, נא להכניס שנית";
    private static final String RESULT_ERROR_PHONE = "מס' טלפון אינו נכון";
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
                if(!isEmpty(nonProfitName) && !isEmpty(nonProfitAddress) && !isEmpty(contactName) && !isEmpty(contactPhone) ){
                    if(contactPhone.getText().toString().length() < 9) {
                        Toast.makeText(getApplicationContext(), RESULT_ERROR_PHONE, Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences(RegistrationActivity.NON_PROFIT, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Association.KEY_NAME, nonProfitName.getText().toString());
                    editor.putString(Association.KEY_CONTACT, contactName.getText().toString());
                    editor.putString(Association.KEY_PHONE, contactPhone.getText().toString());

                    LatLng latLng = getLocationFromAddress(nonProfitAddress.getText().toString());
                    if(latLng == null) {
                        Toast.makeText(getApplicationContext(), RESULT_UNKNOWN_LOCATION, Toast.LENGTH_LONG).show();
                        return;
                    }

                    else {
                        editor.putFloat(Association.KEY_LAT, (float) latLng.latitude);
                        editor.putFloat(Association.KEY_LNG, (float) latLng.longitude);
                    }

                    new CheckForNonProfitListingTask(editor, nonProfitName.getText().toString()).execute();
                }
            }
        });

    }

    private LatLng getLocationFromAddress(String strAddress){

        Locale lHebrew = new Locale.Builder().setLanguage("he").build();
        Geocoder geocoder = new Geocoder(this, lHebrew);
        List<Address> address;
        try {
            address = geocoder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isEmpty(EditText field) {
        boolean empty = false;
        if(field.getText().toString().trim().equals("")){
            empty = true;
            field.setError(" נא למלא שדה זה ");

        }
        return empty;
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
        int uuid;

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
            //TODO check with DB for non profit name
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            uuid = 1;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean listed){
            if(progress != null) progress.dismiss();
            if(listed) {
                editor.putString(Association.KEY_UUID, String.valueOf(uuid));
                editor.apply();
                finish(RESULT_OK);
            }
            else
                Toast.makeText(getApplicationContext(), RESULT_UNKNOWN_NON_PROFIT, Toast.LENGTH_LONG).show();

        }
    }
}
