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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gilshe on 3/31/17.
 */

public class DonationsManager {

    private static final String TAG = DonationsManager.class.getSimpleName();
    private final Map<String, Donation> mDonations;
    private static DonationsManager instance;
    private final StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private OnCounterChangeListener mListener;

    private DonationsManager(OnCounterChangeListener listener){
        mDonations = new LinkedHashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mListener = listener;
        mStorageRef = FirebaseStorage.getInstance().getReference();

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
        final Donor donor = Donor.get();
        Log.d(TAG, "add donation to donor: " + donor.getId());

        // upload to database
        donation.setState(Donation.State.AVAILABLE); // db state

        mDatabase
                .child(Constants.DB_DONATION)
                .child(donationId)
                .setValue(donation);

        // add to donor's ref
        mDatabase
                .child(Constants.DB_DONOR_DONATION)
                .child(donor.getId())
                .child(Constants.DB_DONATION)
                .child(donationId)
                .setValue(true);


        donation.setState(Donation.State.DONOR); // local state only
        mDonations.put(donationId, donation);

    }


    public void returnDonation(final Donation donation) {
        mDonations.remove(donation.getId());
        final Donor donor = Donor.get();

        mDatabase
                .child(Constants.DB_DONATION)
                .child(donation.getId())
                .removeValue();

        //remove from donor ref
        mDatabase
                .child(Constants.DB_DONOR_DONATION)
                .child(donor.getId())
                .child(Constants.DB_DONATION)
                .child(donation.getId())
                .removeValue();

        //delete image from storage
        removeImage(donation.getId());

        //TODO remove from non_profit that owns donation
    }

    public void removeImages(Set<String> donationToRemove) {
        for(String name: donationToRemove)
            removeImage(name);
    }

    private void removeImage(String name) {
        mStorageRef.child(name).delete();
    }

    public void update(final String donationId, final String description, final String calenderStr) {
        final Donation donation = mDonations.get(donationId);

        if(donation.getDescription().equals(description) &&
                donation.getCalendar().equals(calenderStr))
            return;

        donation.setDescription(description);
        donation.setCalendar(calenderStr);

        Map<String, Object> updates = new HashMap<>();
        updates.put(Donation.K_DESCRIPTION, description);
        updates.put(Donation.K_CALENDAR, calenderStr);

        DatabaseReference donationRef = mDatabase
                .child(Constants.DB_DONATION)
                .child(donationId);

        donationRef.updateChildren(updates);
        AdapterManager.get().updateDataSourceAll();

    }

    public void clear() {
        instance = null;
    }

    public void updateProfile(Context context) {
        final Donor donor = Donor.get(context);
        for(Donation d : mDonations.values())
            donor.setProfileInfo(d);

        AdapterManager.get().updateDataSourceAll();
    }

    public void updateImageUrl(Donation donation, String imageUrl) {
        Donation d = mDonations.get(donation.getId());
        d.setImageUrl(imageUrl);

        mDatabase
                .child(Constants.DB_DONATION)
                .child(donation.getId())
                .child(Donation.K_IMAGE)
                .setValue(imageUrl);

        AdapterManager.get().updateDataSourceAll();

    }


    private class FetchDataTask extends AsyncTask<Void, Void, Void> {

        private ValueEventListener getDonationsFromDataBase = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                final Donor donor = Donor.get();
                Map<String, Donation> myDonations = new HashMap<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    try {
                        Donation current = child.getValue(Donation.class);
                        if(!donor.isOwner(current))
                            continue;

                        donor.setProfileInfo(current);
                        myDonations.put(current.getId(), current);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                updateDataSource(myDonations);
                mListener.updateViewCounters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        @Override
        protected Void doInBackground(Void... params) {

            Query myDonations = mDatabase
                    .child(Constants.DB_DONATION);

            myDonations.addListenerForSingleValueEvent(getDonationsFromDataBase);
            return null;
        }
    }

    private void updateDataSource(Map<String, Donation> newData) {
        synchronized (mDonations){
            mDonations.clear();
            mDonations.putAll(newData);
        }
    }
}
