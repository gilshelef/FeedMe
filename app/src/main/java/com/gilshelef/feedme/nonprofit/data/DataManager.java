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
 */

public class DataManager {

    private Map<String, Donation> donations; // holding only available and saved items
    private Map<String, Donation> ownedDonations;
    private static DataManager instance;
    private boolean initialized;
    private final NonProfit nonProfit;
    private OnCounterChangeListener listener;
    private static String TAG = DataManager.class.getSimpleName();

    private DataManager(Context context){
        donations = new LinkedHashMap<>();
        ownedDonations = new LinkedHashMap<>();
        initialized = false;
        this.listener = (OnCounterChangeListener)context;
        this.nonProfit = NonProfit.get(context);
        new FetchDataTask(new OnResult() {
            @Override
            public void onResult() {
                initialized = true;
                AdapterManager.get().updateDataSourceAll();
                listener.updateViewCounters();
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

//    private void getDonationsFromFile(Context context){
//        try {
//            // Load data
//            String jsonString = loadJsonFromAsset("donations.json", context);
//            JSONObject json = new JSONObject(jsonString);
//            JSONArray donations = json.getJSONArray("donations");
//
//            // Get Donation objects from data
//            for(int i = 0; i < donations.length(); i++){
//                JSONObject obj = donations.getJSONObject(i);
//                Donation donation = new Donation(obj);
//
//                //TODO assuming donations from data base arrive as available/owned, saved donations locally
//                String state = obj.getString("state");
//                if(state.equals(AVAILABLE))
//                    donation.setState(Donation.State.AVAILABLE);
//                else donation.setState(Donation.State.SAVED);
//
//                String id = donation.getId();
//                this.donations.put(id, donation);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String loadJsonFromAsset(String filename, Context context) {
//        String json;
//
//        try {
//            InputStream is = context.getAssets().open(filename);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            json = new String(buffer, "UTF-8");
//        }
//        catch (java.io.IOException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//
//        return json;
//    }

    public List<Donation> getSaved(Context context) {
        final List<Donation> saved = new ArrayList<>();

        OnResult callback = new OnResult(){
            @Override
            public void onResult() {
                for(Donation d: donations.values())
                    if(d.isSaved())
                        saved.add(d);
            }
        };

        if(!initialized)
            new FetchDataTask(callback).execute();

        else callback.onResult();
        return saved;

    }

    public List<Donation> getAll(Context context) {
        final List<Donation> all = new ArrayList<>();

        OnResult callback = new OnResult(){
            @Override
            public void onResult() {
                all.addAll(donations.values());
            }
        };
        if(!initialized)
            new FetchDataTask(callback).execute();

        else callback.onResult();
        return all;
    }

    public List<Donation> getInCart() {
        final List<Donation> inCart = new ArrayList<>();
        for(Donation d: donations.values())
            if(d.getInCart())
                inCart.add(d);
        return inCart;
    }

    public List<Donation> getOwned() {
        final List<Donation> owned = new ArrayList<>();
        owned.addAll(ownedDonations.values());
        return owned;
    }

    public void saveEvent(String donationId) {
        if(!donations.containsKey(donationId))
            return;
        Donation d = donations.get(donationId);
        d.setState(Donation.State.SAVED);
    }

    public void unSaveEvent(String donationId) {
        if(!donations.containsKey(donationId))
            return;
        Donation d = donations.get(donationId);
        d.setState(Donation.State.AVAILABLE);
    }

    public int addToCartEvent(String donationId) {
        if(!donations.containsKey(donationId))
            return 0;
        Donation donation = donations.get(donationId);
        int added = donation.getInCart() ? 0: 1;
        donation.setInCart(true);
        return added;
    }

    public int removeFromCartEvent(String donationId) {
        if(!donations.containsKey(donationId))
            return 0;
        Donation donation = donations.get(donationId);
        int removed = donation.getInCart() ? -1 : 0;
        donation.setInCart(false);
        return removed;
    }

    public void ownedEvent(List<String> ownedIds) {
        for(String id: ownedIds) {
            Donation d = donations.get(id);
            if(d == null)
                continue;
            d.setState(Donation.State.OWNED);
            ownedDonations.put(d.getId(), d);
        }

        donations.keySet().removeAll(ownedIds);
        AdapterManager.get().updateDataSourceAll();
    }

    public static void applyFilter(Filter filter) {
        // TODO
    }

    public void returnOwnedDonation(String donationId) {
        if(!ownedDonations.containsKey(donationId))
            return;

        Donation donation = ownedDonations.get(donationId);
        donation.setState(Donation.State.AVAILABLE);
        donation.setInCart(false);
        ownedDonations.remove(donationId);
        donations.put(donationId, donation);
        AdapterManager.get().updateDataSourceAll();

    }

    public static void clear() {
        instance = null;
    }

    private void removeDonation(String donationId) {
        if(donations.containsKey(donationId))
            donations.remove(donationId);
        if(ownedDonations.containsKey(donationId))
            ownedDonations.remove(donationId);
    }

    private class FetchDataTask extends AsyncTask<Void, Void, Void> {
        private final OnResult mCallback;

        FetchDataTask(OnResult callback) {
            this.mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DB_DONATION);

            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "onChildAdded");
                    final Donation donation = dataSnapshot.getValue(Donation.class);
                    donation.setCalendar(dataSnapshot.child(Donation.K_CALENDAR).getValue().toString());

                    //if donation not available (taken or unavailable)
                    if(!donation.isAvailable())
                        return;

                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.DB_DONOR)
                            .child(donation.getDonorId())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot donor) {
                                    Log.d(TAG, "onDataChange");
                                    updateDonor(donation, donor);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "onChildChanged");
                    //donation has changed in the data base
                    final String donationId = dataSnapshot.getKey();
                    final Donation newDonation = dataSnapshot.getValue(Donation.class);

                    Donation donation;
                    if(donations.containsKey(donationId))
                        donation = donations.get(donationId);
                    else donation = ownedDonations.get(donationId);

                    if(newDonation.isUnavailable()) // when donor removes donation
                        removeDonation(donationId);
                    if(newDonation.isTaken() && !nonProfit.isOwned(newDonation)) //another nonprofit took donation
                        removeDonation(donationId);
                    if(nonProfit.isOwned(newDonation)) // state in db is TAKEN
                        newDonation.setState(Donation.State.OWNED);

                    if(newDonation.isAvailable() && donation == null){ // donation has been removed
                        FirebaseDatabase.getInstance().getReference()
                                .child(Constants.DB_DONOR)
                                .child(newDonation.getDonorId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot donor) {
                                        Log.d(TAG, "onDataChange");
                                        updateDonor(newDonation, donor);
                                        donations.put(newDonation.getId(), newDonation);
                                        AdapterManager.get().updateDataSourceAll();
                                        listener.updateViewCounters();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }
                    else if(donation != null) donation.update(newDonation);
                    AdapterManager.get().updateDataSourceAll();
                    listener.updateViewCounters();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved");
                    //donations has been removed
                    final String donationId = dataSnapshot.getKey();
                    donations.remove(donationId);
                    AdapterManager.get().updateDataSourceAll();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            return null;
        }

        private void updateDonor(Donation donation, DataSnapshot donor) {
            if(donor.getValue() == null)
                return;

            String hebrewType = donor.child(Donor.K_TYPE).child(Type.K_HEBREW).getValue().toString();
            donation.setType(TypeManager.get().getType(hebrewType));
            donation.setPhone(donor.child(Donor.K_PHONE).getValue().toString());
            donation.setFirstName(donor.child(Donor.K_FIRST_NAME).getValue().toString());
            donation.setLastName(donor.child(Donor.K_LAST_NAME).getValue().toString());

            DataSnapshot pos = donor.child(Donor.K_POSITION);
            double lat = Double.valueOf(pos.child(Donor.K_LAT).getValue().toString());
            double lng = Double.valueOf(pos.child(Donor.K_LNG).getValue().toString());

            donation.setPosition(new LatLng(lat, lng));
            donation.setBusinessName(donor.child(Donor.K_BUSINESS).getValue().toString());

            if(nonProfit.isOwned(donation)) {
                donation.setState(Donation.State.OWNED);
                ownedDonations.put(donation.getId(), donation);
            }
            else donations.put(donation.getId(), donation);
            AdapterManager.get().updateDataSourceAll();
            listener.updateViewCounters();
        }

        @Override
        protected void onPostExecute(Void v) {
            if(mCallback != null)
                mCallback.onResult();
        }
    }
}
