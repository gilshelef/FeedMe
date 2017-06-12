package com.gilshelef.feedme.nonprofit.data;

import android.content.Context;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final DatabaseHelper mDatabaseHelper;
    private final OnCounterChangeListener mListener;

    private DataManager(Context context){
        mDonations = new ConcurrentHashMap<>();
        mNonProfit = NonProfit.get(context);
        mListener = (OnCounterChangeListener)context;
        mDatabaseHelper = new DatabaseHelper();
        mDatabaseHelper.fetchData();
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
        for(Donation d: mDonations.values())
            if(d.isSaved())
                saved.add(d);
        return saved;

    }

    public List<Donation> getAll() {
        final List<Donation> all = new ArrayList<>();
        for(Donation d: mDonations.values()) {
            if (d.isAvailable() || d.isSaved())
                all.add(d);
        }
        return all;
    }

    public List<Donation> getInCart() {
        final List<Donation> inCart = new ArrayList<>();
        for (Donation d : mDonations.values())
            if (d.getInCart())
                inCart.add(d);

        return inCart;
    }

    public List<Donation> getOwned() {
        final List<Donation> owned = new ArrayList<>();
        for(Donation d: mDonations.values())
            if(mNonProfit.isOwner(d))
                owned.add(d);


        return owned;
    }

    public void saveEvent(String donationId) {
        Donation d = mDonations.get(donationId);
        if(d == null) return;
        d.setState(Donation.State.SAVED);
    }

    public void unSaveEvent(String donationId) { // save and unsave are only for available donations
        Donation d = mDonations.get(donationId);
        if(d == null) return;
        d.setState(Donation.State.AVAILABLE);

    }

    public void addToCartEvent(String donationId) {
        Donation donation = mDonations.get(donationId);
        if(donation == null) return;
        donation.setInCart(true);

    }

    public void removeFromCartEvent(String donationId) {
        Donation donation = mDonations.get(donationId);
        if(donation == null) return;
        donation.setInCart(false);
        mListener.updateViewCounters();
    }

    public void ownedEvent(List<String> ownedIds) {
        for(String id: ownedIds) {
            Donation d = mDonations.get(id);
            if(d == null) continue;

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
        Donation donation = mDonations.get(donationId);
        if(donation == null) return;
        mDatabaseHelper.returnOwnedDonation(donation);
        donation.setState(Donation.State.AVAILABLE);
        donation.setInCart(false);
        donation.setNonProfitId(null);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }

    public static void clear() {
        synchronized (DataManager.class) {
            instance = null;
        }
    }


    /**
     * add donation locally.
     * this event can be  triggered with update of donor's info
     * @param donation
     */
    private void newDonationEvent(Donation donation) {
        mDonations.put(donation.getId(), donation);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }

    /**
     * removes donation with 'id' locally
     * @param id
     */
    private void removeDonation(String id) {
        if (mDonations.containsKey(id)) {
            mDonations.remove(id);
            mDatabaseHelper.donationRemoved(id);
            AdapterManager.get().updateDataSourceAll();
            mListener.updateViewCounters();
        }

    }

    /**
     * event triggered when non profit marks that a donation has been taken
     * @param donationId
     */
    public void takenEvent(String donationId) {
        mDatabaseHelper.takenEvent(donationId);
        removeDonation(donationId);
        AdapterManager.get().updateDataSourceAll();
        mListener.updateViewCounters();
    }


    /**
     * async task to fetch data from database.
     * creates listeners for donation db, when new donations are added
     * creates listeners for donors db reference when a donor updates his profile.
     *
     */
    private class DatabaseHelper {
        private final DatabaseReference mDatabaseRef;

        private DatabaseHelper() {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        }

        void fetchData() {
            mDatabaseRef.child(Constants.DB_DONATION)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {
                            final Donation donation = dataSnapshot.getValue(Donation.class);

                            //fetch donor's profile info
                            mDatabaseRef
                                    .child(Constants.DB_DONOR)
                                    .child(donation.getDonorId())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot1) {

                                            Donor donor = dataSnapshot1.getValue(Donor.class);

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
                            final Donation newDonation = dataSnapshot.getValue(Donation.class);
                            final Donation donation = mDonations.get(newDonation.getId());

                            if(donation == null)
                                return;

                            donation.update(newDonation);

                            //if another non-profit took donation, remove donation from cart
                            if(!donation.isAvailable() && !mNonProfit.isOwner(donation))
                                donation.setInCart(false);

                            AdapterManager.get().updateDataSourceAll();
                            mListener.updateViewCounters();

                        }


                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            //mDonations has been removed
                            final String donationId = dataSnapshot.getKey();
                            removeDonation(donationId);
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }

        void takenEvent(String donationId) {
            Donation donation = mDonations.get(donationId);
            if(donation == null) return;
            mDatabaseRef
                    .child(Constants.DB_DONATION)
                    .child(donationId)
                    .removeValue();
            mDatabaseRef
                    .child(Constants.DB_DONOR_DONATION)
                    .child(donation.getDonorId())
                    .child(Constants.DB_DONATION)
                    .child(donationId)
                    .setValue(Donation.State.TAKEN);
        }

        void returnOwnedDonation(Donation donation) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Donation.K_STATE, Donation.State.AVAILABLE);
            updates.put(Donation.K_NON_PROFIT_ID, null);
            mDatabaseRef
                    .child(Constants.DB_DONATION)
                    .child(donation.getId())
                    .updateChildren(updates);

        }

        void donationRemoved(String donationId) {
            mDatabaseRef
                    .child(Constants.DB_NON_PROFIT)
                    .child(mNonProfit.getId())
                    .child(Constants.DB_DONATION)
                    .child(donationId)
                    .setValue(null);
        }
    }
}
