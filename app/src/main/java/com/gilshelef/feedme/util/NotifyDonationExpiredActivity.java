package com.gilshelef.feedme.util;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TimePicker;

import com.gilshelef.feedme.donors.activities.DonorMainActivity;
import com.gilshelef.feedme.donors.fragments.TimePickerFragment;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.Calendar;

/**
 * Created by gilshe on 5/31/17.
 */

public class NotifyDonationExpiredActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, AppCompatMaterialAlertDialog.OnAction {
    private static final String TAG = "TIME";
    public static final int ACTION_PRESSED = 0;
    public static final int ACTION_DISMISS = 1;
    public static final String ACTION = "action";
    private String mDonationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "NotifyTimePassesActivity onCreate");

        Bundle extras = getIntent().getExtras();
        mDonationId = extras.getString(Donation.K_ID);
        int action = extras.getInt(ACTION, ACTION_PRESSED);

        if(action == ACTION_DISMISS)
            dismissEvent();

        else pressedEvent();


    }

    private void pressedEvent() {
        Log.d(TAG, "pressedEvent");
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialog = AppCompatMaterialAlertDialog.getInstance();
        dialog.show(fm, "dialog");
    }


    private void dismissEvent() {
        Log.d(TAG,"dismissEvent");
        sendUpdate(DonorMainActivity.ACTION_REMOVE_DONATION);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        sendUpdate(DonorMainActivity.ACTION_UPDATE_TIME, cal);
    }

    private void sendUpdate(String action) {
        sendUpdate(action, null);
    }

    private void sendUpdate(String action, Calendar cal) {
        Context context = NotifyDonationExpiredActivity.this;
        Intent intent = new Intent(context, DonorMainActivity.class);
        intent.setAction(action);
        intent.putExtra(Donation.K_ID, mDonationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(cal != null)
            addCalendar(intent, cal);
        context.startActivity(intent);
    }

    private void addCalendar(Intent intent, Calendar cal) {
        Donation donation = new Donation();
        donation.calendar = cal;
        intent.putExtra(Donation.K_CALENDAR, donation.calenderToString());
    }

    @Override
    public void onUpdate() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialog = new TimePickerFragment();
        dialog.show(fm, "dialog");
    }

    @Override
    public void onCancel() {
        sendUpdate(DonorMainActivity.ACTION_REMOVE_DONATION);
    }

}
