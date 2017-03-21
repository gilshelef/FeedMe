package com.gilshelef.feedme.fragments;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.adapters.CartAdapter;
import com.gilshelef.feedme.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.data.DataManager;
import com.gilshelef.feedme.data.Donation;

import java.util.List;

/**
 * Created by gilshe on 3/17/17.
 */

public class CartFragment extends BaseFragment implements View.OnClickListener, CartAdapter.OnUpdateCount {
    public static final String TAG = CartFragment.class.getSimpleName();
    private FloatingActionButton checkoutBtn;
    private CoordinatorLayout mCoordinator;
    private TextView itemCountUI;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ImageButton exit = (ImageButton) rootView.findViewById(R.id.cart_exit_btn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mCoordinator = (CoordinatorLayout) rootView.findViewById(R.id.coordinator);
        checkoutBtn = (FloatingActionButton) rootView.findViewById(R.id.cart_checkout);
        checkoutBtn.setOnClickListener(this);
        itemCountUI = (TextView) rootView.findViewById(R.id.item_count);
        updateItemsCount();

        ((ToggleHomeBar) getActivity()).drawAppBar(false);
        ((ToggleHomeBar) getActivity()).drawHomeBar(false);

        return rootView;
    }

    @Override
    public void updateItemsCount() {
        if (itemCountUI == null) return;
        final int count = mDataSource.size();
        if (count < 0)
            Log.e(TAG, "ERROR!! number of items in bag is = " + count);
        if (count <= 0) {
            itemCountUI.setVisibility(View.INVISIBLE);
            checkoutBtn.setVisibility(View.GONE);
        } else {
            itemCountUI.setVisibility(View.VISIBLE);
            checkoutBtn.setVisibility(View.VISIBLE);
            itemCountUI.setText(Integer.toString(count));
        }
    }

    @Override
    protected RecycledBaseAdapter getAdapter() {
        return new CartAdapter(getActivity(), mDataSource, this);
    }

    @Override
    protected List<Donation> getDataSource() {
        return DataManager.get(getActivity()).getInCart();
    }

    @Override
    protected View inflate(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ToggleHomeBar) getActivity()).drawAppBar(true);
        ((ToggleHomeBar) getActivity()).drawHomeBar(true);
    }

    @Override
    public void onClick(View v) {
        Snackbar snackbar = Snackbar
                .make(mCoordinator, "The donation are yours", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar returned = Snackbar.make(mCoordinator, "Donations returned", Snackbar.LENGTH_SHORT);
                        returned.show();
                    }
                });

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != DISMISS_EVENT_ACTION) { // take action!
                    //TODO notify data base
                    // notify service
                }
            }
        });

        snackbar.show();
    }
}




