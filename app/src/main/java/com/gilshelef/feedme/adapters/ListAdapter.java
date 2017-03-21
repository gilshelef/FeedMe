package com.gilshelef.feedme.adapters;

import android.app.Activity;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

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
    protected void styleSelectedItem(ViewHolder holder, Donation donation) {
        if(donation.inCart()) {
            holder.itemView.setBackground(mActivity.getDrawable(R.color.selected));
            holder.image.setBackground(mActivity.getDrawable(R.color.selected));
        }
        else {
            holder.itemView.setBackground(mActivity.getDrawable(R.color.lightPrimaryColor));
            holder.image.setBackground(mActivity.getDrawable(R.color.lightPrimaryColor));
        }
    }

    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getAll(mActivity));
    }

}
