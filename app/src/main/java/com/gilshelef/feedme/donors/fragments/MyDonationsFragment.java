package com.gilshelef.feedme.donors.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.donors.data.DonationsManager;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.fragments.BaseFragment;
import com.gilshelef.feedme.util.OnUpdateCount;

import java.util.List;

/**
 * Created by gilshe on 3/29/17.
 */

public class MyDonationsFragment extends BaseFragment implements OnUpdateCount {
    public static final String TAG = MyDonationsFragment.class.getSimpleName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    protected RecycledBaseAdapter getAdapter() {
        return new MyDonationsAdapter(getActivity(), mDataSource, this);
    }

    @Override
    protected List<Donation> getDataSource() {
        return DonationsManager.get().getAll();
    }

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.donors_fragment_my_donations, container, false);
    }


    @Override
    public void onSaveEvent(Donation donation) {
    }

    @Override
    public void onUnSaveEvent(Donation donation) {
    }

    @Override
    public void onDetailsEvent(View v, Donation donation){
        ((OnDetailsListener)getActivity()).onDetails(v, donation);
    }

    @Override
    public void updateItemsCount() {
    }

}
