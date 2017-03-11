package com.gilshelef.feedme.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.adapters.CartAdapter;
import com.gilshelef.feedme.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/23/17.
 */

public class CartFragment extends BaseFragment{

    @Override
    protected RecycledBaseAdapter getAdapter() {
        return new CartAdapter(getActivity(), mDataSource, this);
    }

    @Override
    protected List<Donation> getDataSource() {
        return DataManager.get(getActivity()).getSaved(getActivity());
    }

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_cart, container, false);


    }


    /*
    *  in cart tab, if donation if selected and saved and onSaveEvent occurs than before removing
    * from the screen we need to call unselected Event
    * */
    @Override
    public void onSaveEvent(Donation donation) {
        if(donation.isSaved() && donation.isSelected()) {
            ((OnSelectedEvent) getActivity()).onSelectedEvent(donation.getId(), false);
            donation.setSelected(false);
        }

        super.onSaveEvent(donation);
    }


}
