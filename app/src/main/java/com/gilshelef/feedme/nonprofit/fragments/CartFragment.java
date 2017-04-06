package com.gilshelef.feedme.nonprofit.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import com.gilshelef.feedme.nonprofit.adapters.CartAdapter;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.util.OnUpdateCount;

import java.util.List;

/**
 * Created by gilshe on 3/17/17.
 */

public class CartFragment extends BaseFragment implements View.OnClickListener, OnUpdateCount {
    public static final String TAG = CartFragment.class.getSimpleName();
    private FloatingActionButton mCheckoutBtn;
    private CoordinatorLayout mCoordinator;
    private TextView mItemCountUI;

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
        mCheckoutBtn = (FloatingActionButton) rootView.findViewById(R.id.cart_checkout);
        mCheckoutBtn.setOnClickListener(this);
        mItemCountUI = (TextView) rootView.findViewById(R.id.item_count);
        updateItemsCount();

        ((ToggleHomeBar) getActivity()).drawAppBar(false);
        ((ToggleHomeBar) getActivity()).drawHomeBar(false);

        return rootView;
    }

    @Override
    public void updateItemsCount() {
        if (mItemCountUI == null) return;
        final int count = mDataSource.size();
        if (count < 0)
            Log.e(TAG, "ERROR!! number of items in bag is = " + count);
        if (count <= 0) {
            mItemCountUI.setVisibility(View.INVISIBLE);
            mCheckoutBtn.setVisibility(View.GONE);
        } else {
            mItemCountUI.setVisibility(View.VISIBLE);
            mCheckoutBtn.setVisibility(View.VISIBLE);
            mItemCountUI.setText(Integer.toString(count));
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
                .make(mCoordinator, "Taking Donations", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar returned = Snackbar.make(mCoordinator, "Donations returned!", Snackbar.LENGTH_SHORT);
                        returned.show();

                        returned.setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                            }
                        });
                    }
                });

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if(event != DISMISS_EVENT_CONSECUTIVE && event != DISMISS_EVENT_ACTION)
                    new TakeDonationsTask().execute();
            }
        });

        snackbar.show();
    }

    private class TakeDonationsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute(){
            progress = new ProgressDialog(getActivity());
            progress.setMessage("Taking Donations...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            if(progress != null) progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //TODO notify data base with my donations - mysql
            //TODO notify service
            //TODO change checkout event only for donations that were TAKEN!
            //TODO notify db that donation taken - donation id + non profit id
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            DataManager.get(getActivity()).ownedEvent(mDataSource);
            ((OnCounterChangeListener)getActivity()).updateViewCounters();
            if(progress != null) progress.dismiss();
        }

    }
}




