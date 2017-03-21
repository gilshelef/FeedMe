package com.gilshelef.feedme.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gilshelef.feedme.R;

/**
 * Created by gilshe on 3/2/17.
 */
public class AccountFragment extends Fragment {
    public static final String TAG = AccountFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ToggleHomeBar) getActivity()).drawHomeBar(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ToggleHomeBar) getActivity()).drawHomeBar(true);
    }

}
