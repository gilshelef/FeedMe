package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gilshe on 3/21/17.
 */

public class OwnedAdapter extends RecycledBaseAdapter {

    public OwnedAdapter(Activity activity, List<Donation> dataSource, OnActionEvent listener) {
        super(activity, dataSource, listener);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    protected void styleListItem(ItemViewHolder holder, Donation donation) {
        holder.save.setEnabled(false);
        holder.save.setAlpha(0.7f);
        holder.itemView.setAlpha(0.7f);
    }

    @Override
    protected void onItemDismiss(final Donation donation) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.dialog_return_donation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO notify data base, service!
                DataManager.get(mActivity).returnOwnedDonation(donation.getId());
                Toast.makeText(mActivity, R.string.returned_donation_successfully, Toast.LENGTH_LONG).show();
                ((OnCounterChangeListener) mActivity).updateViewCounters(); // owned and home
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DataManager.get(mActivity).ownedEvent(Arrays.asList(donation));
                ((OnCounterChangeListener) mActivity).updateViewCounters(); // owned and home
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

    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getOwned());
    }
}
