package com.gilshelef.feedme.nonprofit.data;

import android.content.Context;
import android.os.AsyncTask;

import com.gilshelef.feedme.nonprofit.adapters.AdapterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
    private static final String AVAILABLE = "0";
    private boolean initialized;

    private DataManager(Context context){
        donations = new LinkedHashMap<>();
        ownedDonations = new LinkedHashMap<>();
        initialized = false;
        new FetchDataTask(context, null).execute();
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

    private void getDonationsFromFile(Context context){
        try {
            // Load data
            String jsonString = loadJsonFromAsset("donations.json", context);
            JSONObject json = new JSONObject(jsonString);
            JSONArray donations = json.getJSONArray("donations");

            // Get Donation objects from data
            for(int i = 0; i < donations.length(); i++){
                JSONObject obj = donations.getJSONObject(i);
                Donation donation = new Donation(obj);

                //TODO assuming donations from data base arrive as available/owned, saved donations locally
                String state = obj.getString("state");
                if(state.equals(AVAILABLE))
                    donation.setState(Donation.State.AVAILABLE);
                else donation.setState(Donation.State.SAVED);

                String id = donation.getId();
                this.donations.put(id, donation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String loadJsonFromAsset(String filename, Context context) {
        String json;

        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

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
            new FetchDataTask(context, callback).execute();

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
            new FetchDataTask(context, callback).execute();

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

    public void ownedEvent(List<Donation> ownedDonations) {
        List<String> donationIds = new LinkedList<>();
        for(Donation d: ownedDonations) {
            donationIds.add(d.getId());
            d.setState(Donation.State.OWNED);
            this.ownedDonations.put(d.getId(), d);
        }

        donations.keySet().removeAll(donationIds);
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


    private class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final Context mContext;
        private final OnResult mCallback;

        FetchDataTask(Context context, OnResult callback) {
            this.mContext = context;
            this.mCallback = callback;
        }

        @Override
        protected Integer doInBackground(String... params) {
            getDonationsFromFile(mContext);
            initialized = true;
            if(donations.size() == 0)
                return 0;
            return 1; // successful
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(mCallback != null)
                mCallback.onResult();
        }
    }
}
