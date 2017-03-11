package com.gilshelef.feedme.adapters;

import android.content.Context;

import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/26/17.
 */

public class CartAdapter extends RecycledBaseAdapter {

    static final String TAG = CartAdapter.class.getSimpleName();

    public CartAdapter(Context context, List<Donation> dataSource, OnActionEvent listener) {
        super(context, dataSource, listener);
    }

    @Override
    void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mContext).getSaved(mContext));
    }

}
