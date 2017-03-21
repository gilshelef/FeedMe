package com.gilshelef.feedme.adapters;

import android.app.Activity;

import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 3/17/17.
 */

public class CartAdapter extends RecycledBaseAdapter {

    public final String TAG = CartAdapter.class.getSimpleName();

    public CartAdapter(Activity activity, List<Donation> dataSource, OnUpdateCount listener) {
        super(activity, dataSource, listener);
    }


    @Override
    public void updateDataSource() {
        mDataSource.clear();
        mDataSource.addAll(DataManager.get(mActivity).getInCart());
        ((OnUpdateCount)mListener).updateItemsCount();
    }

    @Override
    protected void styleSelectedItem(ViewHolder itemView, Donation donation) {

    }

    public interface OnUpdateCount extends OnActionEvent {
        void updateItemsCount();
    }
}
