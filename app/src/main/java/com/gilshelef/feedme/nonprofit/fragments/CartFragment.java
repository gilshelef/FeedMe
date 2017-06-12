package com.gilshelef.feedme.nonprofit.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gilshelef.feedme.R;
import com.gilshelef.feedme.nonprofit.adapters.CartAdapter;
import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;
import com.gilshelef.feedme.nonprofit.data.DataManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.NonProfit;
import com.gilshelef.feedme.util.Constants;
import com.gilshelef.feedme.util.OnUpdateCount;
import com.gilshelef.feedme.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
//        if (count < 0) {
//            Log.e(TAG, "ERROR!! number of items in bag is = " + count);
//        }
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
        NonProfit nonProfit = NonProfit.get(getContext());
        if(!nonProfit.isAuthorized())
            alertUnauthorized();
        else showSnackbar();
    }

    private void alertUnauthorized() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unauthorize_message);
        builder.setPositiveButton(R.string.ok, null);
        TextView title = Util.buildTitleView(getContext(), getString(R.string.sorry));
        builder.setCustomTitle(title);
        if(!getActivity().isFinishing())
            builder.show();

    }

    private void showSnackbar() {
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


    /**
     *  task that notify data base that current non profit wants to take these donations.
     * the task return only the donation that were in-fact taken and marked with nonprofit's id.
     * add each donation taken to non-profit's data base reference in order to display in app (owned fragment)
     */
    private class TakeDonationsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress;
        private List<String> takenDonations;
        private AtomicInteger counter;
        private final int counterBarrier;

        TakeDonationsTask(){
            takenDonations = new LinkedList<>();
            counter = new AtomicInteger(0);
            counterBarrier = mDataSource.size();
        }

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
            final String nonProfitId = NonProfit.get(getActivity()).getId();

            DatabaseReference donationRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_DONATION);
            final DatabaseReference nonProfitRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DB_NON_PROFIT)
                    .child(nonProfitId)
                    .child(Constants.DB_DONATION);

            for (final Donation donation : mDataSource){
                donationRef.child(donation.getId())
                        .runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Donation d = mutableData.getValue(Donation.class);
                                if (d == null || !d.isAvailable())
                                    return Transaction.abort();
                                if(d.isAvailable()){
                                    d.setState(Donation.State.OWNED);
                                    d.setNonProfitId(nonProfitId);
                                    mutableData.setValue(d);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot dataSnapshot) {
                                if(committed){
                                    Donation current = dataSnapshot.getValue(Donation.class);
                                    takenDonations.add(current.getId());
                                    nonProfitRef.child(current.getId()).setValue(true);
                                    mLogger.ownDonation(current.getId());
                                    notify(R.string.owned_donations);
                                }else {
                                    notify(R.string.missed_donation);

                                }


                                if(counter.incrementAndGet() == counterBarrier)
                                    onTransactionsComplete();
                            }

                            private void notify(int msgId) {
                                final String msg = getActivity().getString(msgId);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), msg,Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }

            return null;
        }

        private void onTransactionsComplete(){
            if(progress != null && progress.isShowing()) progress.dismiss();
            DataManager.get(getActivity()).ownedEvent(takenDonations);
        }
    }
}




