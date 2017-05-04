package com.gilshelef.feedme.donors.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 3/31/17.
 */

public class DonationsManager {

    private static final String TAG = DonationsManager.class.getSimpleName();
    private Map<String, Donation> mDonations;
    private static DonationsManager instance;
    private DatabaseReference mDatabase;
    private OnCounterChangeListener mListener;

    private DonationsManager(OnCounterChangeListener listener){
        mDonations = new LinkedHashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mListener = listener;
        new FetchDataTask().execute();
    }

    public static DonationsManager get(OnCounterChangeListener listener) {
        if (instance == null) {
            synchronized (DonationsManager.class) {
                if (instance == null)
                    return build(listener);
            }
        }
        return instance;
    }

    public static DonationsManager get() {
        return instance;
    }

    private static DonationsManager build(OnCounterChangeListener listener) {
        instance = new DonationsManager(listener);
        return instance;
    }

    public List<Donation> getAll() {
        final List<Donation> all = new ArrayList<>();
        all.addAll(mDonations.values());
        return all;
    }

    public void newDonationEvent(final Donation donation) {
        final String donationId = donation.getId();
        donation.setState(Donation.State.DONOR); // local state only
        mDonations.put(donationId, donation);

        new Runnable(){
            final Donor donor = Donor.get();
            @Override
            public void run() {
                Log.d(TAG, "add donation to donor: " + donor.getId());
                donation.setState(Donation.State.AVAILABLE); // db state

                //upload to db
                mDatabase
                        .child(Constants.DB_DONATION)
                        .child(donationId)
                        .setValue(donation);

                // add to donor's ref
                mDatabase
                        .child(Constants.DB_DONOR)
                        .child(donor.getId())
                        .child(Constants.DB_DONATION)
                        .child(donationId)
                        .setValue(true);

                updateDonationsCount(donor.getDonationCount());
            }
        }.run();
    }

    public void returnDonation(final Donation donation) {
        mDonations.remove(donation.getId());

        new Runnable(){
            final Donor donor = Donor.get();
            @Override
            public void run() {
                //remove from mDonations ref
                mDatabase
                        .child(Constants.DB_DONATION)
                        .child(donation.getId())
                        .child(Donation.K_STATE)
                        .setValue(null);

                //remove from donor ref
                mDatabase
                        .child(Constants.DB_DONOR)
                        .child(Constants.DB_DONATION)
                        .child(donation.getId())
                        .removeValue();

                updateDonationsCount(donor.getDonationCount());

            }
        }.run();

        //TODO remove from non_profit that owns donation
    }

    private void updateDonationsCount(int donationCount) {
        //update donation count
        mDatabase.child(Constants.DB_DONOR)
                .child(Donor.get().getId())
                .child(Donor.K_DONATION_COUNT)
                .setValue(donationCount);
    }

    public void update(final String donationId, final String description, final String calenderStr) {
        final Donation donation = mDonations.get(donationId);
        donation.setDescription(description);
        donation.setCalendar(calenderStr);
        AdapterManager.get().updateDataSourceAll();

        // update in database
        new Runnable(){
            @Override
            public void run() {
                DatabaseReference donationRef = mDatabase
                        .child(Constants.DB_DONATION)
                        .child(donationId);
                donationRef.child(Donation.K_DESCRIPTION).setValue(description);
                donationRef.child(Donation.K_CALENDAR).setValue(calenderStr);
            }
        }.run();
    }

    public void clear() {
        instance = null;
    }

    public void updateProfile(Context context) {
        final Donor donor = Donor.get(context);
        new Runnable(){
            @Override
            public void run() {
                for(Donation d : mDonations.values()){
                    d.setType(donor.getDonationType());
                    d.setPhone(donor.getPhone());
                    d.setFirstName(donor.getFirstName());
                    d.setLastName(donor.getLastName());
                    d.setPosition(donor.getPosition());
                    d.setBusinessName(donor.getBusinessName());
                }
                AdapterManager.get().updateDataSourceAll();
            }
        }.run();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, Void> {

        private ValueEventListener getDonationsFromDataBase = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                final Donor donor = Donor.get();
                mDonations.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    try {
                        //TODO change to comment below
//                        Donation d = child.getValue(Donation.class);
                        String donorId = child.child(Donation.K_DONOR_ID).getValue().toString();
                        if(!donorId.equals(donor.getId()))
                            continue;

                        Donation.State donationState = Donation.State.valueOf(child.child(Donation.K_STATE).getValue().toString());
                        if(donationState.equals(Donation.State.UNAVAILABLE))
                            continue;

                        Donation current = new Donation();
                        current.phone = donor.getPhone();
                        current.firstName = donor.getFirstName();
                        current.lastName = donor.getLastName();
                        current.position = donor.getPosition();
                        current.businessName = donor.getBusinessName();
                        current.setId(child.child(Donation.K_ID).getValue().toString());
                        current.calendar = Donation.stringToCalender(child.child(Donation.K_CALENDAR).getValue().toString());
                        current.description = child.child(Donation.K_DESCRIPTION).getValue().toString();
                        current.imageUrl = child.child(Donation.K_IMAGE).getValue().toString();
                        current.setState(donationState);
                        current.setInCart(Boolean.valueOf(child.child("inCart").getValue().toString()));
                        current.type = donor.getDonationType();
                        current.donorId = donor.getId();
                        if(child.child(Donation.K_NON_PROFIT_ID).getValue() != null)
                            current.setNonProfitId(child.child(Donation.K_NON_PROFIT_ID).getValue().toString());
                        mDonations.put(current.getId(), current);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                mListener.updateViewCounters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        @Override
        protected Void doInBackground(Void... params) {

            Query myDonations = mDatabase
                    .child(Constants.DB_DONATION)
                    .equalTo(Donor.get().getId());

            myDonations.addListenerForSingleValueEvent(getDonationsFromDataBase);
//            mDatabase
//                    .child(Constants.DB_DONATION)
//                    .addListenerForSingleValueEvent(getDonationsFromDataBase);
            return null;
        }
    }
}
