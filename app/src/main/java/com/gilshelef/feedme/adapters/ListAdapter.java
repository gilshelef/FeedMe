package com.gilshelef.feedme.adapters;

import android.content.Context;

import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/21/17.
 */
public class ListAdapter extends RecycledBaseAdapter {

    static final String TAG = ListAdapter.class.getSimpleName();

    public ListAdapter(Context context, List<Donation> dataSource, OnActionEvent listener) {
        super(context, dataSource, listener);
    }

    @Override
    void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mContext).getAll(mContext));
    }
}
