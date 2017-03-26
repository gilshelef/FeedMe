package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;

import com.gilshelef.feedme.nonprofit.OnCounterChangeListener;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/26/17.
 */

public class SaveAdapter extends RecycledBaseAdapter {

    public static final String TAG = SaveAdapter.class.getSimpleName();

    public SaveAdapter(Activity activity, List<Donation> dataSource, OnActionEvent listener) {
        super(activity, dataSource, listener);
    }

    @Override
    protected void styleListItem(ItemViewHolder holder, Donation donation) {
        if(donation.inCart())
            super.setSelected(holder);
        else
            super.setUnSelected(holder);
    }

    @Override
    protected void onItemDismiss(Donation donation) {
        DataManager.get(mActivity).unSaveEvent(donation.getId());
        ((OnCounterChangeListener) mActivity).updateViewCounters();
    }

    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getSaved(mActivity));
    }

}
