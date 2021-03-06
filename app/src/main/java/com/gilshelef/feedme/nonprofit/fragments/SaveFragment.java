package com.gilshelef.feedme.nonprofit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.adapters.SaveAdapter;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 2/23/17.
 */

public class SaveFragment extends BaseFragment{

    public static final String TAG = SaveFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ((ToggleHomeBar)getActivity()).drawHomeBar(false);
        return rootView;
    }


        @Override
    protected RecycledBaseAdapter getAdapter() {
        return new SaveAdapter(getActivity(), mDataSource, this);
    }

    @Override
    protected List<Donation> getDataSource() {
        return DataManager.get(getActivity()).getSaved();
    }

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_save, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ToggleHomeBar) getActivity()).drawHomeBar(true);
    }

}
