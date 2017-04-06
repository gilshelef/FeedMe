package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;

import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/21/17.
 */
public class ListAdapter extends RecycledBaseAdapter {

    static final String TAG = ListAdapter.class.getSimpleName();

    public ListAdapter(Activity activity, List<Donation> dataSource, OnActionEvent listener) {
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
    }

    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getAll(mActivity));
    }

}
