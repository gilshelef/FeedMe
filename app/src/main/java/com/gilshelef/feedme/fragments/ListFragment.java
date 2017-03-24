package com.gilshelef.feedme.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.adapters.ListAdapter;
import com.gilshelef.feedme.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;


/**
 * Created by gilshe on 2/22/17.
 */
public class ListFragment extends BaseFragment {
    public static final String TAG = ListFragment.class.getSimpleName();


    @Override
    protected RecycledBaseAdapter getAdapter() {
        return new ListAdapter(getActivity(), mDataSource, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {}

    @Override
    protected List<Donation> getDataSource() {
        return DataManager.get(getActivity()).getAll(getActivity());
    }

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

}
