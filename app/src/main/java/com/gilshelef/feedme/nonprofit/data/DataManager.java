package com.gilshelef.feedme.nonprofit.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gilshelef.feedme.donors.data.Donor;
import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
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

    private final Map<String, Donation> mDonations; // holding only available and saved items
//    private Map<String, Donation> ownedDonations;
    private static DataManager instance;
//    private boolean initialized;
    private final NonProfit mNonProfit;
    private OnCounterChangeListener mListener;
    private static String TAG = DataManager.class.getSimpleName();

    private DataManager(Context context){
        mDonations = new LinkedHashMap<>();
//        ownedDonations = new LinkedHashMap<>();
//        initialized = false;
        this.mListener = (OnCounterChangeListener)context;
        this.mNonProfit = NonProfit.get(context);
        new FetchDataTask(new OnResult() {
            @Override
            public void onResult() {
//                initialized = true;
                AdapterManager.get().updateDataSourceAll();
                mListener.updateViewCounters();
            }
        }).execute();
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

    public List<Donation> getSaved(Context context) {
        final List<Donation> saved = new ArrayList<>();

        synchronized (mDonations){
            for(Donation d: mDonations.values())
                if(d.isSaved())
                    saved.add(d);
        }

        return saved;

    }

    public List<Donation> getAll(Context context) {
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
        for(Donation d: mDonations.values())
            if(d.getInCart())
                inCart.add(d);
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
        if(!mDonations.containsKey(donationId))
            return;
        Donation d = mDonations.get(donationId);
        d.setState(Donation.State.SAVED);
    }

    public void unSaveEvent(String donationId) {
        if(!mDonations.containsKey(donationId))
            return;
        Donation d = mDonations.get(donationId);
        d.setState(Donation.State.AVAILABLE);
    }

    public void addToCartEvent(String donationId) {
        if(!mDonations.containsKey(donationId))
            return;
        Donation donation = mDonations.get(donationId);
        donation.setInCart(true);

    }

    public void removeFromCartEvent(String donationId) {
        if(!mDonations.containsKey(donationId))
            return;
        Donation donation = mDonations.get(donationId);
        donation.setInCart(false);
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

        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }

    public static void applyFilter(Filter filter) {
        // TODO
    }

    public void returnOwnedDonation(String donationId) {
        if(!mDonations.containsKey(donationId))
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


    private class FetchDataTask extends AsyncTask<Void, Void, Void> {
        private final OnResult mCallback;
        private final DatabaseReference mDatabaseRef;

        FetchDataTask(OnResult callback) {
            mCallback = callback;
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

                    //calendar is excluded
//                    donation.setCalendar(dataSnapshot.child(Donation.K_CALENDAR).getValue().toString());//TODO check if calendar need change

                    //fetch donor's profile info
                    mDatabaseRef
                            .child(Constants.DB_DONOR)
                            .child(donation.getDonorId())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    Log.d(TAG, "onDataChange");

                                    Donor donor = dataSnapshot1.getValue(Donor.class); // TODO check for errors
                                    Type donationType = TypeManager.get().getType(
                                            dataSnapshot1.child(Donor.K_TYPE).child(Type.K_HEBREW).getValue().toString());
                                    donor.setType(donationType);

                                    DataSnapshot pos = dataSnapshot1.child(Donor.K_POSITION);
                                    LatLng latLng = new LatLng(pos.child(Donor.K_LAT).getValue(Double.class),pos.child(Donor.K_LNG).getValue(Double.class));
                                    donor.setPosition(latLng);
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

                    if(!newDonation.getState().equals(donation.getState()))
                        changeStateEvent(donation, newDonation);

                    else donation.update(newDonation);
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

//        private void updateDonor(Donation donation, DataSnapshot donor) {
//            if(donor.getValue() == null)
//                return;
//
//            String hebrewType = donor.child(Donor.K_TYPE).child(Type.K_HEBREW).getValue().toString();
//            donation.setType(TypeManager.get().getType(hebrewType));
//            donation.setPhone(donor.child(Donor.K_PHONE).getValue().toString());
//            donation.setFirstName(donor.child(Donor.K_FIRST_NAME).getValue().toString());
//            donation.setLastName(donor.child(Donor.K_LAST_NAME).getValue().toString());
//
//            DataSnapshot pos = donor.child(Donor.K_POSITION);
//            double lat = Double.valueOf(pos.child(Donor.K_LAT).getValue().toString());
//            double lng = Double.valueOf(pos.child(Donor.K_LNG).getValue().toString());
//
//            donation.setPosition(new LatLng(lat, lng));
//            donation.setBusinessName(donor.child(Donor.K_BUSINESS).getValue().toString());
//
//            if(mNonProfit.isOwner(donation)) {
//                donation.setState(Donation.State.OWNED);
//                ownedDonations.put(donation.getId(), donation);
//            }
//            else mDonations.put(donation.getId(), donation);
//            AdapterManager.get().updateDataSourceAll();
//            mListener.updateViewCounters();
//        }

        @Override
        protected void onPostExecute(Void v) {
            if(mCallback != null)
                mCallback.onResult();
        }
    }

    private void changeStateEvent(Donation donation, Donation newDonation) {
        //TODO
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
        if(mDonations.containsKey(id))
            mDonations.remove(id);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }


}
