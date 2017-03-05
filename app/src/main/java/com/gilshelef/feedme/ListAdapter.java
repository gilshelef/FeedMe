package com.gilshelef.feedme;

import android.content.Context;

import java.util.List;

/**
 * Created by gilshe on 2/21/17.
 */
class ListAdapter extends RecycledBaseAdapter {

    static final String TAG = ListAdapter.class.getSimpleName();

    ListAdapter(Context context, List<Donation> dataSource, OnActionEvent listener) {
        super(context, dataSource, listener);
    }

    @Override
    int getListItemLayout() {
        return R.layout.list_row;
    }

    @Override
    void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mContext).getAll(mContext));
    }
}
