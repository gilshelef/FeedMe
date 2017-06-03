package com.gilshelef.feedme.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.Donation;

/**
 * Created by gilshe on 5/31/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "TIME";
    public static final String ACTION = "feedme.donation.alarm";

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d(TAG, "onReceive, time passes for donation");
        if(i.getAction().equals(ACTION)) {
            Bundle extras = i.getExtras();
            String donationId = extras.getString(Donation.K_ID);

            Intent intent = new Intent(context, NotifyDonationExpiredActivity.class);
            intent.putExtra(NotifyDonationExpiredActivity.ACTION, NotifyDonationExpiredActivity.ACTION_PRESSED);
            intent.putExtra(Donation.K_ID, donationId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, NotifyDonationExpiredActivity.ACTION_PRESSED, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.donate_icon)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Your donation is about to expire! Tap here to update.")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);


            //dismiss notification event
            Intent deleteIntent = new Intent(context, NotifyDonationExpiredActivity.class);
            deleteIntent.putExtra(NotifyDonationExpiredActivity.ACTION, NotifyDonationExpiredActivity.ACTION_DISMISS);
            deleteIntent.putExtra(Donation.K_ID, donationId);
            PendingIntent deletePIntent = PendingIntent.getActivity(context,
                    NotifyDonationExpiredActivity.ACTION_DISMISS, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
            builder.setDeleteIntent(deletePIntent);


            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
    }
}
