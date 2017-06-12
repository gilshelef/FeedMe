package com.gilshelef.feedme.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by gilshe on 5/31/17.
 */

public class Util {

    private static AlarmManager mAlarmManager;

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
        if(donation == null)
            return;

        Intent activate = new Intent(context, AlarmReceiver.class);
        Calendar calendar = donation.calendar;
        activate.setAction(AlarmReceiver.ACTION);
        activate.putExtra(Donation.K_ID, donation.getId());

        int alertId = donation.getId().hashCode();
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alertId, activate, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarms = getAlarmManager(context);
        alarms.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        Log.d("TIME", "scheduled alarm to: " + calendar.getTime() + " with id: " + alertId);

    }

    private static AlarmManager getAlarmManager(Context context) {
        synchronized (Util.class) {
            if (mAlarmManager == null)
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return mAlarmManager;
    }


    public static void unScheduleAlarm(Context context, String donationId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION);
        intent.putExtra(Donation.K_ID, donationId);

        int alertId = donationId.hashCode();
        boolean alarmUp = (PendingIntent.getBroadcast(context, alertId, intent, PendingIntent.FLAG_NO_CREATE) != null);

        PendingIntent deleteIntent = PendingIntent.getBroadcast(context, alertId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarms = getAlarmManager(context);
        alarms.cancel(deleteIntent);
        Log.d("TIME", "cancel alarm with id " + alertId + " alarm is up? " + alarmUp);
    }

    public static String loadPreference(Context context) {
        SharedPreferences shp = context.getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = shp.getString("Language","he");
        Locale myLocale = new Locale(language);

        Configuration config = new Configuration();
        config.setLocale(myLocale);

        //manually set layout direction to a LTR location
        config.setLayoutDirection(new Locale("en"));

        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        String locale = resources.getConfiguration().locale.getDisplayName();
        return locale;
    }

}
