package com.gilshelef.feedme.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.Calendar;

import static com.gilshelef.feedme.donors.fragments.AddDonationFragment.TAG;

/**
 * Created by gilshe on 5/31/17.
 */

public class Util {

    public static TextView buildTitleView(Context context, String text){
        TextView title = new TextView(context);
        title.setText(text);
        title.setElegantTextHeight(true);
        title.setPadding(Constants.PADDING_LEFT, Constants.PADDING_TOP, Constants.PADDING_RIGHT, Constants.PADDING_BOTTOM);
        title.setGravity(Gravity.START);
        title.setTextSize(22);
        return title;
    }
    public static EditText buildInputView(Context context, String hint) {
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint(hint);
        input.setPadding(Constants.PADDING_LEFT, Constants.PADDING_TOP, Constants.PADDING_RIGHT, Constants.PADDING_TOP);
        return input;

    }

    public static ProgressDialog buildProgressDialog(Context context) {
        ProgressDialog progress = new ProgressDialog(context);
        TextView title = buildTitleView(context, context.getString(R.string.please_wait));
        progress.setCustomTitle(title);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        return progress;
    }

    public static AlertDialog.Builder notifyEmailSent(Context context, DialogInterface.OnClickListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.email_authorization_sent);
        builder.setPositiveButton(R.string.ok, callback);
        builder.setCancelable(false);
        builder.show();
        return builder;
    }

    public static void scheduleAlarm(Context context, Donation donation) {
        Calendar calendar = donation.calendar;
        calendar.add(Calendar.MINUTE, 1);
        Log.d(TAG, "scheduled alarm to: " + calendar.getTime());
        Intent activate = new Intent(context, Alarm.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, activate, 0);
        activate.putExtra(Donation.K_ID, donation.getId());
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }


}
