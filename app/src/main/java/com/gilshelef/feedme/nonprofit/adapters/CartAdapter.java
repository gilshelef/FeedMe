package com.gilshelef.feedme.nonprofit.adapters;

import android.app.Activity;

import com.gilshelef.feedme.nonprofit.OnCounterChangeListener;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

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
    protected void styleListItem(ItemViewHolder itemView, Donation donation) {

    }

    @Override
    protected void onItemDismiss(Donation donation) {
        DataManager.get(mActivity).removeFromCartEvent(donation.getId());
        ((OnUpdateCount) mListener).updateItemsCount();
        ((OnCounterChangeListener) mActivity).updateViewCounters();

    }

    public interface OnUpdateCount extends OnActionEvent {
        void updateItemsCount();
    }
}
