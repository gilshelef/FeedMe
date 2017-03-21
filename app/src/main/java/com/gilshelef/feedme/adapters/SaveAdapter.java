package com.gilshelef.feedme.adapters;

import android.app.Activity;

import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

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
    protected void styleSelectedItem(ViewHolder itemView, Donation donation) {

    }

    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getSaved(mActivity));
    }

}
