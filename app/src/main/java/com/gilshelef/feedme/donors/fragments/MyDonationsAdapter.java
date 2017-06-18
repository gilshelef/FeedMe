package com.gilshelef.feedme.donors.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.nonprofit.adapters.ItemViewHolder;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Logger;
import com.gilshelef.feedme.util.OnUpdateCount;
import com.gilshelef.feedme.util.Util;

import java.util.List;

/**
 * Created by gilshe on 3/29/17.
 */

public class MyDonationsAdapter extends RecycledBaseAdapter {

    private final OnCounterChangeListener onCounterListener;

    public MyDonationsAdapter(Activity activity, List<Donation> dataSource, OnUpdateCount listener) {
        super(activity, dataSource, listener);
        this.onCounterListener = (OnCounterChangeListener) activity;
    }

    @Override
    public void updateDataSource() {
        synchronized (mLock) {
            mDataSource.clear();
            mDataSource.addAll(DonationsManager.get(onCounterListener).getAll());
        }
    }

    @Override
    protected boolean isDonorAdapter() {
        return true;
    }

    @Override
    protected void styleListItem(ItemViewHolder itemView, Donation donation) {
        itemView.save.setVisibility(View.GONE);
        if(donation.calenderToString() != null) {
            itemView.distance.setText(donation.calenderToString());
            itemView.distance.setVisibility(View.VISIBLE);
        }
        else itemView.distance.setVisibility(View.GONE);
    }

    @Override
    protected void onItemDismiss(final Donation donation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        TextView title = Util.buildTitleView(mActivity, mActivity.getString(R.string.remove_donation));
        builder.setCustomTitle(title);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Donor.get(mActivity).updateDonationCount(mActivity, -1);
                DonationsManager.get(onCounterListener).returnDonation(donation.getId());
                Logger.get(mActivity).returnDonation(donation.getId());
                Toast.makeText(mActivity, R.string.remove_donation_successfully, Toast.LENGTH_LONG).show();
                Util.unScheduleAlarm(mActivity, donation.getId());
                ((OnCounterChangeListener) mActivity).updateViewCounters(); // owned and home
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateDataSource();
                notifyDataSetChanged();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }
}
