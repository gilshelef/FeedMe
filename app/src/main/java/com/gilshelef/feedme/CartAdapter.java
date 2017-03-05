package com.gilshelef.feedme;

import android.content.Context;

import java.util.List;

/**
 * Created by gilshe on 2/26/17.
 */

class CartAdapter extends RecycledBaseAdapter {

    static final String TAG = CartAdapter.class.getSimpleName();

    CartAdapter(Context context, List<Donation> dataSource, OnActionEvent listener) {
        super(context, dataSource, listener);
    }

    @Override
    int getListItemLayout() {
        return R.layout.list_row;
    }

    @Override
    void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mContext).getSaved(mContext));
    }

}
