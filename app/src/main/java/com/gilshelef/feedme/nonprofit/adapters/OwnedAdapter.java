package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.util.Util;

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
        holder.save.setVisibility(View.GONE);
    }

    @Override
    protected void onItemDismiss(final Donation donation) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        TextView title = Util.buildTitleView(mActivity, "שים לב");
        builder.setCustomTitle(title);
        builder.setMessage(R.string.dialog_return_donation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DataManager.get(mActivity).returnOwnedDonation(donation.getId());
                Toast.makeText(mActivity, R.string.returned_donation_successfully, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DataManager.get(mActivity).ownedEvent(Arrays.asList(donation.getId()));
            }
        });

        title.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_error_black_24dp, 0, 0, 0);
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
