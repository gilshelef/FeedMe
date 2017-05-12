package com.gilshelef.feedme.nonprofit.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.types.Other;
import com.gilshelef.feedme.nonprofit.data.types.Type;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 2/25/17.
 * class handles all data related issues
 *
 * Donation - Available, Saved, Taken, Owned
 */

public class DataManager {

    private static String TAG = DataManager.class.getSimpleName();
    private final Map<String, Donation> mDonations; // holding only available and saved items
    private static DataManager instance;
    private final NonProfit mNonProfit;
    private OnCounterChangeListener mListener;

    private DataManager(Context context){
        mDonations = new LinkedHashMap<>();
        mNonProfit = NonProfit.get(context);
        mListener = (OnCounterChangeListener)context;
        new FetchDataTask().execute();
    }

    public static DataManager get(Context context) {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null)
                    return build(context);
            }
        }
        return instance;
    }

    private static DataManager build(Context context) {
        instance = new DataManager(context);
        return instance;
    }

    public List<Donation> getSaved() {
        final List<Donation> saved = new ArrayList<>();
        synchronized (mDonations){
            for(Donation d: mDonations.values())
                if(d.isSaved())
                    saved.add(d);
        }

        return saved;

    }

    public List<Donation> getAll() {
        final List<Donation> all = new ArrayList<>();
        synchronized (mDonations){
            for(Donation d: mDonations.values())
                if(d.isAvailable() || d.isSaved())
                    all.add(d);
        }

        return all;
    }

    public List<Donation> getInCart() {
        final List<Donation> inCart = new ArrayList<>();
        synchronized (mDonations) {
            for (Donation d : mDonations.values())
                if (d.getInCart())
                    inCart.add(d);
        }
        return inCart;
    }

    public List<Donation> getOwned() {
        final List<Donation> owned = new ArrayList<>();

        synchronized (mDonations){
            for(Donation d: mDonations.values())
                if(mNonProfit.isOwner(d))
                    owned.add(d);
        }

        return owned;
    }

    public void saveEvent(String donationId) {
        if (!mDonations.containsKey(donationId))
            return;
        Donation d = mDonations.get(donationId);
        d.setState(Donation.State.SAVED);
    }

    public void unSaveEvent(String donationId) { // save and unsave are only for available donations
        if (!mDonations.containsKey(donationId))
            return;
        Donation d = mDonations.get(donationId);
        d.setState(Donation.State.AVAILABLE);

    }

    public void addToCartEvent(String donationId) {

        if (!mDonations.containsKey(donationId))
            return;
        Donation donation = mDonations.get(donationId);
        donation.setInCart(true);

    }

    public void removeFromCartEvent(String donationId) {
        if (!mDonations.containsKey(donationId))
            return;

        Donation donation = mDonations.get(donationId);
        donation.setInCart(false);
        mListener.updateViewCounters();
    }

    public void ownedEvent(List<String> ownedIds) {
        for(String id: ownedIds) {
            Donation d = mDonations.get(id);
            if(d == null)
                continue;

            d.setState(Donation.State.OWNED);
            d.setNonProfitId(mNonProfit.getId());
            d.setInCart(false);
        }

        if(ownedIds.size() > 0)  {
            AdapterManager.get().updateDataSourceAll();
            mListener.updateViewCounters();
        }
    }

    public static void applyFilter(Filter filter) {
        // TODO
    }

    public void returnOwnedDonation(String donationId) {

        if (!mDonations.containsKey(donationId))
            return;

        Donation donation = mDonations.get(donationId);
        donation.setState(Donation.State.AVAILABLE);
        donation.setInCart(false);
        donation.setNonProfitId(null);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }

    public static void clear() {
        instance = null;
    }

    public void takenEvent(String donationId) {

        FirebaseDatabase.getInstance().getReference()
                .child(Constants.DB_DONATION)
                .child(donationId)
                .removeValue();

        if(!mDonations.containsKey(donationId))
            return;

        mDonations.remove(donationId);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }


    private class FetchDataTask extends AsyncTask<Void, Void, Void> {

        private final DatabaseReference mDatabaseRef;


        FetchDataTask() {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        }

        @Override
        protected Void doInBackground(Void... params) {

            mDatabaseRef.child(Constants.DB_DONATION)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "onChildAdded");
                    final Donation donation = dataSnapshot.getValue(Donation.class);

                    //fetch donor's profile info
                    mDatabaseRef
                            .child(Constants.DB_DONOR)
                            .child(donation.getDonorId())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    Log.d(TAG, "onDataChange");

                                    Donor donor = dataSnapshot1.getValue(Donor.class); // TODO check for errors

                                    if(donor == null) // donor removed registration
                                        return;
                                    Type donationType;
                                    Object type = dataSnapshot1.child(Donor.K_TYPE).child(Type.K_HEBREW).getValue();
                                    if(type == null)
                                        donationType = TypeManager.get().getType(Other.TAG);
                                    else donationType = TypeManager.get().getType(type.toString());
                                    donor.setType(donationType);

                                    DataSnapshot pos = dataSnapshot1.child(Donor.K_POSITION);
                                    if(pos != null) {
                                        LatLng latLng = new LatLng(pos.child(Donor.K_LAT).getValue(Double.class), pos.child(Donor.K_LNG).getValue(Double.class));
                                        donor.setPosition(latLng);
                                    }
                                    donor.setProfileInfo(donation);
                                    newDonationEvent(donation);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "onChildChanged");

                    final Donation newDonation = dataSnapshot.getValue(Donation.class);
                    final Donation donation = getDonation(newDonation.getId());

                    donation.update(newDonation);
                    AdapterManager.get().updateDataSourceAll();
                    mListener.updateViewCounters();
                }

//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
//                    Log.d(TAG, "onChildChanged");
//
//                    //donation has changed in the data base
//                    final String donationId = dataSnapshot.getKey();
//                    final Donation newDonation = dataSnapshot.getValue(Donation.class);
//
//                    Donation donation;
//                    if(mDonations.containsKey(donationId))
//                        donation = mDonations.get(donationId);
//                    else donation = ownedDonations.get(donationId);
//
//                    //TODO
//                    if(newDonation.isUnavailable()) // when donor removes donation
//                        removeDonation(donationId);
//                    if(newDonation.isTaken() && !mNonProfit.isOwner(newDonation)) //another nonprofit took donation
//                        removeDonation(donationId);
//                    if(mNonProfit.isOwner(newDonation)) // state in db is TAKEN
//                        newDonation.setState(Donation.State.OWNED);
//
//                    if(newDonation.isAvailable() && donation == null){ // donation has been removed
//                        FirebaseDatabase.getInstance().getReference()
//                                .child(Constants.DB_DONOR)
//                                .child(newDonation.getDonorId())
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot1) {
//                                        Log.d(TAG, "onDataChange");
//                                        updateDonor(newDonation, dataSnapshot1);
//                                        mDonations.put(newDonation.getId(), newDonation);
//                                        AdapterManager.get().updateDataSourceAll();
//                                        mListener.updateViewCounters();
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                    }
//                                });
//                    }
//                    else if(donation != null) donation.update(newDonation);
//                    AdapterManager.get().updateDataSourceAll();
//                    mListener.updateViewCounters();
//                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved");
                    //mDonations has been removed
                    final String donationId = dataSnapshot.getKey();
                    removeDonation(donationId);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            return null;
        }
    }


    private Donation getDonation(String id) {
        Donation donation = null;
        if(mDonations.containsKey(id))
            donation = mDonations.get(id);
        return  donation;
    }

    private void newDonationEvent(Donation donation) {
        mDonations.put(donation.getId(), donation);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();

    }

    private void removeDonation(String id) {

        if (mDonations.containsKey(id)) {
            mDonations.remove(id);
            AdapterManager.get().updateDataSourceAll();
            mListener.updateViewCounters();
        }

    }


}
