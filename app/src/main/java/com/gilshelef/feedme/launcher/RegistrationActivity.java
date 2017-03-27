package com.gilshelef.feedme.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.activities.DonorMainActivity;
import com.gilshelef.feedme.donors.activities.RegistrationDonorActivity;
import com.gilshelef.feedme.nonprofit.activities.NonProfitMainActivity;
import com.gilshelef.feedme.nonprofit.activities.RegistrationNonProfitActivity;

/**
 * Created by gilshe on 3/21/17.
 */

public class RegistrationActivity extends AppCompatActivity {
    public static final String NON_PROFIT = "nonProfit";
    public static final String DONOR = "donor";
    public static final String PREFS = "prefs";

    private float x1,x2;
    static final int MIN_DISTANCE = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean registeredAsNonProfit = sharedPref.getBoolean(NON_PROFIT, false);
        boolean registeredAsDonor = sharedPref.getBoolean(DONOR, false);

        if(registeredAsNonProfit){
            Intent intent = new Intent(RegistrationActivity.this, NonProfitMainActivity.class);
            startActivity(intent);
        }

        else if(registeredAsDonor){
            Intent intent = new Intent(RegistrationActivity.this, DonorMainActivity.class);
            startActivity(intent);
        }

        else{
            setContentView(R.layout.activity_registration);
            Button donors = (Button) findViewById(R.id.donor);
            Button nonProfit = (Button) findViewById(R.id.non_profit);

            nonProfit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegistrationActivity.this, RegistrationNonProfitActivity.class);
                    startActivityForResult(intent, RegistrationNonProfitActivity.REGISTER_NON_PROFIT);
                }
            });

            donors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegistrationActivity.this, RegistrationDonorActivity.class);
                    startActivityForResult(intent, RegistrationDonorActivity.REGISTER_DONOR);
                }
            });
        }
    }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == RegistrationDonorActivity.REGISTER_DONOR) {
           if (resultCode == RESULT_OK) {
               markRegister(DONOR);
               Intent intent = new Intent(RegistrationActivity.this, DonorMainActivity.class);
               startActivity(intent);
           }
       }

       else if(requestCode == RegistrationNonProfitActivity.REGISTER_NON_PROFIT){
           if(resultCode == RESULT_OK){
               markRegister(NON_PROFIT);
               Intent intent = new Intent(RegistrationActivity.this, NonProfitMainActivity.class);
               startActivity(intent);
           }
       }

   }

    private void markRegister(String key) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(key, true).apply(); // mark as listed
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show ();
                }
                else {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }

}
