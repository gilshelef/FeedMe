package com.gilshelef.feedme.donors.data;

import android.os.AsyncTask;
import android.util.Log;

import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;
import com.gilshelef.feedme.nonprofit.data.Donation;
import com.gilshelef.feedme.nonprofit.data.types.TypeManager;
import com.gilshelef.feedme.nonprofit.fragments.OnCounterChangeListener;
import com.gilshelef.feedme.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gilshe on 3/31/17.
 */

public class DonationsManager {

    private static final String TAG = DonationsManager.class.getSimpleName();
    private Map<String, Donation> donations;
    private static DonationsManager instance;
    private DatabaseReference mDatabase;
    private OnCounterChangeListener mListener;

    private DonationsManager(OnCounterChangeListener listener){
        donations = new LinkedHashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.mListener = listener;
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
        all.addAll(donations.values());
        return all;
    }

    public void newDonationEvent(final Donation donation) {
        final String donationId = donation.getId();
        donations.put(donationId, donation);

        final Donor donor = Donor.get();
        Log.d("BUG", "add donation to donor: " + donor.getId());
        new Runnable(){
            @Override
            public void run() {
                //upload to db
                mDatabase.child(Constants.DB_DONATION_KEY).child(donationId).setValue(donation);
                mDatabase
                        .child(Constants.DB_DONATION_KEY)
                        .child(donationId)
                        .child(Constants.DB_DONATION_CAL_KEY)
                        .setValue(donation.calenderToString());

                // add to donor's ref
                mDatabase
                        .child(Constants.DB_DONOR_KEY)
                        .child(donor.getId())
                        .child(Constants.DB_DONATION_KEY)
                        .child(donationId)
                        .setValue(true);

                // update donation count
                mDatabase.child(Constants.DB_DONOR_KEY)
                        .child(donor.getId())
                        .child(Constants.DB_DONOR_COUNT_KEY)
                        .setValue(donor.getDonationCount());
            }
        }.run();
    }

    public void returnDonation(final Donation donation) {
        donations.remove(donation.getId());

        final Donor donor = Donor.get();
        donor.addDonation(-1);

        new Runnable(){

            @Override
            public void run() {
                //remove from donations ref
                mDatabase
                        .child(Constants.DB_DONATION_KEY)
                        .child(donation.getId())
                        .child(Constants.DB_DONATION_STATE_KEY)
                        .setValue(Donation.State.UNAVAILABLE);

                //remove from donor ref
                mDatabase
                        .child(Constants.DB_DONOR_KEY)
                        .child(donor.getId())
                        .child(Constants.DB_DONATION_KEY)
                        .child(donation.getId())
                        .removeValue();

                //update donation count
                mDatabase.child(Constants.DB_DONOR_KEY)
                        .child(donor.getId())
                        .child(Constants.DB_DONOR_COUNT_KEY)
                        .setValue(donor.getDonationCount());
            }
        }.run();

        //TODO remove from non_profit that owns donation

    }

    public void update(final String donationId, final String description, final String calenderStr) {
        Donation donation = donations.get(donationId);
        donation.setDescription(description);
        donation.setCalendar(calenderStr);
        AdapterManager.get().updateDataSourceAll();

        // update in database
        new Runnable(){
            @Override
            public void run() {
                DatabaseReference donationRef = mDatabase.child(Constants.DB_DONATION_KEY).child(donationId);
                donationRef.child(Constants.DB_DONATION_DESC_KEY).setValue(description);
                donationRef.child(Constants.DB_DONATION_CAL_KEY).setValue(calenderStr);
            }
        }.run();
    }

    public void clear() {
        instance = null;
    }

    private class FetchDataTask extends AsyncTask<Void, Void, Void> {

        private ValueEventListener getDonationsFromDataBase = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Donation current;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    try {
                        String donorId = child.child("donorId").getValue().toString();
                        if(!donorId.equals(Donor.get().getId()))
                            continue;

                        Donation.State donationState = Donation.State.valueOf(child.child(Constants.DB_DONATION_STATE_KEY).getValue().toString());
                        if(donationState.equals(Donation.State.UNAVAILABLE))
                            continue;

                        current = new Donation();
                        current.phone = child.child("phone").getValue().toString();
                        current.firstName = child.child("firstName").getValue().toString();
                        current.lastName = child.child("lastName").getValue().toString();
                        JSONObject position = new JSONObject(child.child("position").getValue().toString());
                        current.position = new LatLng(position.getDouble("latitude"), position.getDouble("longitude"));
                        current.businessName = child.child("businessName").getValue().toString();
                        current.setId(child.child(Constants.DONATION_ID).getValue().toString());
                        current.calendar = Donation.stringToCalender(child.child(Constants.DB_DONATION_CAL_KEY).getValue().toString());
                        current.description = child.child(Constants.DB_DONATION_DESC_KEY).getValue().toString();
                        current.imageUrl = child.child("imageUrl").getValue().toString();
                        current.setState(donationState);
                        current.setInCart(Boolean.valueOf(child.child("inCart").getValue().toString()));

                        String donationType = child.child("type").child("hebrewName").getValue().toString();
                        current.type = TypeManager.get().getType(donationType);
                        current.donorId = donorId;
                        donations.put(current.getId(), current);
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }
                }

                mListener.updateViewCounters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        @Override
        protected Void doInBackground(Void... params) {
            mDatabase.child(Constants.DB_DONATION_KEY).addListenerForSingleValueEvent(getDonationsFromDataBase);
            return null;
        }
    }
}
