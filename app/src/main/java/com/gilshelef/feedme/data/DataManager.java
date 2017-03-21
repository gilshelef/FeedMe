package com.gilshelef.feedme.data;

import android.content.Context;
import android.os.AsyncTask;

import com.gilshelef.feedme.adapters.AdapterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gilshe on 2/25/17.
 * class handles all data related issues
 */

public class DataManager {

    private static Map<String, Donation> donations; // holding only available and saved items
    private static DataManager instance;
    private static final String AVAILABLE = "0";
    private static boolean initialized;

    private DataManager(){
        donations = new LinkedHashMap<>();
        initialized = false;
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
        instance = new DataManager();
        new FetchDataTask(context, null).execute();
        return instance;
    }

    private static void getDonationsFromFile(Context context){
        initialized = true;
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
                DataManager.donations.put(id, donation);
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

//    public void saveEvent(String id) {
//        Donation d = donations.get(id);
//        if(d.isAvailable())  // available => saved
//            d.setState(Donation.State.SAVED);
//        else if(d.isSaved())  // saved => available
//            d.setState(Donation.State.AVAILABLE);
//
//        AdapterManager.get().updateDataSourceAll();
//    }

    public void saveEvent(String id) {
        Donation d = donations.get(id);
        d.setState(Donation.State.SAVED);
    }

    public void unSaveEvent(String id) {
        Donation d = donations.get(id);
        d.setState(Donation.State.AVAILABLE);}

    public void selectEvent(String id) {
        Donation d = donations.get(id);
        if (!d.isInCart())
            d.setInCart(true);
        else d.setInCart(false);

        AdapterManager.get().updateDataSourceAll();
    }

    public void removeAll(Set<String> items) {
        donations.keySet().removeAll(items);
        AdapterManager.get().updateDataSourceAll();
    }

    public void returnAll(Set<String> selected) {
        for(String id: selected)
            donations.get(id).setInCart(false);
        AdapterManager.get().updateDataSourceAll();

    }

    public static void applyFilter(Filter filter) {
        // TODO
    }

    public List<Donation> getInCart() {
        final List<Donation> inCart = new ArrayList<>();
        for(Donation d: donations.values())
            if(d.inCart())
                inCart.add(d);
        return inCart;
    }

    public Donation getDonation(String donationId) {
        return donations.get(donationId);
    }

    public int addToCartEvent(String donationId) {
        Donation donation = donations.get(donationId);
        int added = donation.inCart() ? 0: 1;
        donation.setInCart(true);
        return added;
    }

    public int removeFromCartEvent(String donationId) {
        Donation donation = donations.get(donationId);
        int removed = donation.inCart() ? -1 : 0;
        donation.setInCart(false);
        return removed;
    }

    private static class FetchDataTask extends AsyncTask<String, Void, Integer> {
        private final Context mContext;
        private final OnResult mCallback;

        FetchDataTask(Context context, OnResult callback) {
            this.mContext = context;
            this.mCallback = callback;
        }

        @Override
        protected Integer doInBackground(String... params) {
            getDonationsFromFile(mContext);
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
